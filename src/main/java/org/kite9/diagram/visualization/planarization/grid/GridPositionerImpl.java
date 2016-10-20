package org.kite9.diagram.visualization.planarization.grid;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.Connected;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.AbstractAnchoringVertex.Anchor;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertex;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertices;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.serialization.CSSConstants;
import org.kite9.framework.serialization.IntegerRangeValue;

/**
 * Tools for helping create Grid structure.  
 * Split out from {@link GroupPhase}.
 * 
 * @author robmoffat
 *
 */
public class GridPositionerImpl implements GridPositioner {

	Map<Container, DiagramElement[][]> placed = new HashMap<>();
	Map<DiagramElement, OPair<BigFraction>> xPositions = new HashMap<>(100);
	Map<DiagramElement, OPair<BigFraction>> yPositions = new HashMap<>(100);
	Map<Container, OPair<Map<BigFraction, Double>>> fracMaps = new HashMap<>();
	
	
	private Dimension calculateGridSize(Container ord) {
		// these are a minimum size, but contents can exceed them and push this out.
		int xSize = (int) ord.getCSSStyleProperty(CSSConstants.GRID_COLUMNS_PROPERTY).getFloatValue();
		int ySize = (int) ord.getCSSStyleProperty(CSSConstants.GRID_ROWS_PROPERTY).getFloatValue();
		
		
		// fit as many elements as possible into the grid
		for (DiagramElement diagramElement : ord.getContents()) {
			if (diagramElement instanceof Connected) {
				IntegerRangeValue xpos = (IntegerRangeValue) diagramElement.getCSSStyleProperty(CSSConstants.GRID_OCCUPIES_X_PROPERTY);
				IntegerRangeValue ypos = (IntegerRangeValue) diagramElement.getCSSStyleProperty(CSSConstants.GRID_OCCUPIES_Y_PROPERTY);
				xSize = Math.max(xpos.getTo()+1, xSize);
				ySize = Math.max(ypos.getTo()+1, ySize);
			}
		}
		
		return new Dimension(xSize, ySize);
	}
	
	
	public DiagramElement[][] placeOnGrid(Container ord) {
		if (placed.containsKey(ord)) {
			return placed.get(ord);
		}
		
		Dimension size = calculateGridSize(ord);
		
		List<DiagramElement> overlaps = new ArrayList<>();
		List<DiagramElement[]> out = new ArrayList<>();
		
		for (int x = 0; x < size.width; x++) {
			DiagramElement[] ys = new DiagramElement[size.height];
			out.add(ys);
		}
		
		
		for (DiagramElement diagramElement : ord.getContents()) {
			if (diagramElement instanceof Connected) {
				IntegerRangeValue xpos = (IntegerRangeValue) diagramElement.getCSSStyleProperty(CSSConstants.GRID_OCCUPIES_X_PROPERTY);
				IntegerRangeValue ypos = (IntegerRangeValue) diagramElement.getCSSStyleProperty(CSSConstants.GRID_OCCUPIES_Y_PROPERTY);	
				
				if ((!xpos.notSet()) && (!ypos.notSet()) && (ensureGrid(out, xpos, ypos, null))) {
					ensureGrid(out, xpos, ypos, diagramElement);
					storeCoordinates(diagramElement, xpos.getFrom(), xpos.getTo(), ypos.getFrom(), ypos.getTo(), size);
				} else {
					overlaps.add(diagramElement);
					
				}
			}
		}
		
		// add remaining/dummy elements elements, by adding extra rows if need be.
		int xr = 0;
		while ((overlaps.size() > 0) || (xr < out.size())) {
			if (xr == out.size()) {
				DiagramElement[] ys = new DiagramElement[size.height];
				out.add(ys);
			}
			
			DiagramElement[] ys = out.get(xr);
			for (int y = 0; y < size.height; y++) {
				if (ys[y] == null) {
					if (!overlaps.isEmpty()) {
						ys[y] = overlaps.remove(overlaps.size()-1);
					} else {
						ys[y] = new GridTemporaryConnected(ord, xr, y);
						modifyContainerContents(ord, ys[y]);
					}
					storeCoordinates(ys[y], xr, xr, y, y, size);
				}
			}
		
			xr++;
		}
		
		DiagramElement[][] done = (DiagramElement[][]) out.toArray(new DiagramElement[out.size()][]);
		placed.put(ord, done);
		return done;
	}


	/**
	 * Deprecated, because we wanted to have immutable containers.
	 */
	@Deprecated
	private void modifyContainerContents(Container ord, DiagramElement d) {
		ord.getContents().add(d);
	}

	
	private void storeCoordinates(DiagramElement diagramElement, int sx, int ex, int sy, int ey, Dimension size) {
		xPositions.put(diagramElement, new OPair<BigFraction>(BigFraction.getReducedFraction(sx, size.width), BigFraction.getReducedFraction(ex+1, size.width)));
		yPositions.put(diagramElement, new OPair<BigFraction>(BigFraction.getReducedFraction(sy, size.height), BigFraction.getReducedFraction(ey+1, size.height)));
	}


	/**
	 * Iterates over the grid squares occupied by the ranges and either checks that they are empty, 
	 * or sets their value.
	 */
	private static boolean ensureGrid(List<DiagramElement[]> out, IntegerRangeValue xpos, IntegerRangeValue ypos, DiagramElement in) {
		// ensure grid is large enough for the elements.
		for (int x = xpos.getFrom(); x < xpos.getTo()+1; x++) {
			DiagramElement[] ys = out.get(x);
			
			for (int y = ypos.getFrom(); y < ypos.getTo()+1; y++) {
				if (in == null) {
					if (ys[y] != null) {
						return false;
					}
				} else {
					ys[y] = in;
				}
			}
			
		}
		
		return true;
	}


	@Override
	public OPair<BigFraction> getGridXPosition(DiagramElement elem) {
		if (!xPositions.containsKey(elem)) {
			throw new Kite9ProcessingException("Would expect the element to have been given a grid position: "+elem);
		}
		
		return xPositions.get(elem);
	}


	@Override
	public OPair<BigFraction> getGridYPosition(DiagramElement elem) {
		if (!yPositions.containsKey(elem)) {
			throw new Kite9ProcessingException("Would expect the element to have been given a grid position: "+elem);
		}
		
		return yPositions.get(elem);
	}


	@Override
	public OPair<Map<BigFraction, Double>> getFracMapForGrid(Container c, RoutableHandler2D rh, ContainerVertices containerVertices) {
		OPair<Map<BigFraction, Double>> out = fracMaps.get(c);
		if (out != null) {
			return out;
		}
		
		RoutingInfo ri = rh.getPlacedPosition(c);
		Bounds xBounds = rh.getBoundsOf(ri, true);
		Bounds yBounds = rh.getBoundsOf(ri, false);
		
		Map<BigFraction, Bounds> left = new HashMap<>(), right  = new HashMap<>() , up  = new HashMap<>() , down  = new HashMap<>();
		
		// work out where this appears in relation to the neighbouring container's positions.
		for (ContainerVertex cv : containerVertices.getAllVertices()) {
			for (Anchor a : cv.getAnchors()) {
				RoutingInfo place = rh.getPlacedPosition(a.getDe());
				Bounds x = rh.getBoundsOf(place, true);
				if (a.getLr() == HPos.RIGHT) {
					expand(left, x, cv.getXOrdinal());
				} else if (a.getLr() == HPos.LEFT) {
					expand(right, x, cv.getXOrdinal());
				}
				
				Bounds y = rh.getBoundsOf(place, false);
				if (a.getUd() == VPos.DOWN) {
					expand(up, y, cv.getYOrdinal());
				} else if (a.getUd() == VPos.UP) {
					expand(down, y, cv.getYOrdinal());
				}
			}
		}
		
		Map<BigFraction, Double> xOut = new HashMap<>();
		xOut.put(BigFraction.ZERO, 0d);
		xOut.put(BigFraction.ONE, 1d);
		for (BigFraction bf : left.keySet()) {
			if (!xOut.containsKey(bf)) {
				Bounds bleft = left.get(bf);
				Bounds bright = right.get(bf);
				
				if (bleft.getDistanceMax() > bright.getDistanceMin()) {
					throw new LogicException("Overlapping bounds in grid");
				}
				
				double midPoint = (bleft.getDistanceMax() + bright.getDistanceMin()) /2d;
				double frac = (midPoint-xBounds.getDistanceMin()) / (xBounds.getDistanceMax() - xBounds.getDistanceMin());
				xOut.put(bf, frac);
			}
		}
		
		Map<BigFraction, Double> yOut = new HashMap<>();
		yOut.put(BigFraction.ZERO, 0d);
		yOut.put(BigFraction.ONE, 1d);

		for (BigFraction bf : up.keySet()) {
			if (!yOut.containsKey(bf)) {
				Bounds bup = up.get(bf);
				Bounds bdown = down.get(bf);
				
				if (bup.getDistanceMax() > bdown.getDistanceMin()) {
					throw new LogicException("Overlapping bounds in grid");
				}

				
				double midPoint = (bup.getDistanceMax() + bdown.getDistanceMin()) /2d;
				double frac = (midPoint - yBounds.getDistanceMin()) / (yBounds.getDistanceMax() - yBounds.getDistanceMin());
				yOut.put(bf, frac);
			}
		}
		
		out = new OPair<Map<BigFraction,Double>>(xOut, yOut);
		fracMaps.put(c, out);
		return out;
	}


	private void expand(Map<BigFraction, Bounds> boundsMap, Bounds newBounds, BigFraction ord) {
		Bounds oldBounds = boundsMap.get(ord);
		if (oldBounds == null) {
			boundsMap.put(ord, newBounds);
		} else {
			boundsMap.put(ord, oldBounds.expand(newBounds));
		}
	}
}
