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
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.framework.common.Kite9ProcessingException;
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
}
