package org.kite9.diagram.visualization.planarization.grid;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
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
	
	
	private Dimension calculateGridSize(Container ord, boolean allowSpanning) {
		// these are a minimum size, but contents can exceed them and push this out.
		int xSize = (int) ord.getCSSStyleProperty(CSSConstants.GRID_COLUMNS_PROPERTY).getFloatValue();
		int ySize = (int) ord.getCSSStyleProperty(CSSConstants.GRID_ROWS_PROPERTY).getFloatValue();
		
		
		// fit as many elements as possible into the grid
		for (DiagramElement diagramElement : ord.getContents()) {
			if (diagramElement instanceof Connected) {
				IntegerRangeValue xpos = getXOccupies(diagramElement);
				IntegerRangeValue ypos = getYOccupies(diagramElement);
				
				if (allowSpanning) {
					xSize = Math.max(xpos.getTo()+1, xSize);
					ySize = Math.max(ypos.getTo()+1, ySize);
				} else {
					xSize = Math.max(xpos.getFrom()+1, xSize);
					ySize = Math.max(ypos.getFrom()+1, ySize);
				}
			}
		}
		
		return new Dimension(xSize, ySize);
	}


	public static IntegerRangeValue getYOccupies(DiagramElement diagramElement) {
		return (IntegerRangeValue) diagramElement.getCSSStyleProperty(CSSConstants.GRID_OCCUPIES_Y_PROPERTY);
	}


	public static IntegerRangeValue getXOccupies(DiagramElement diagramElement) {
		return (IntegerRangeValue) diagramElement.getCSSStyleProperty(CSSConstants.GRID_OCCUPIES_X_PROPERTY);
	}
	
	
	public DiagramElement[][] placeOnGrid(Container ord, boolean allowSpanning) {
		if (placed.containsKey(ord)) {
			return placed.get(ord);
		}
		
		Dimension size = calculateGridSize(ord, allowSpanning);
		
		List<DiagramElement> overlaps = new ArrayList<>();
		List<DiagramElement[]> out = new ArrayList<>();
		
		for (int x = 0; x < size.width; x++) {
			DiagramElement[] ys = new DiagramElement[size.height];
			out.add(ys);
		}
		
		
		for (DiagramElement diagramElement : ord.getContents()) {
			if (diagramElement instanceof Connected) {
				IntegerRangeValue xpos = getXOccupies(diagramElement);
				IntegerRangeValue ypos = getYOccupies(diagramElement);	
				
				if ((!xpos.notSet()) && (!ypos.notSet()) && (ensureGrid(out, xpos, ypos, null, allowSpanning))) {
					ensureGrid(out, xpos, ypos, diagramElement, allowSpanning);
					int xTo = allowSpanning ? xpos.getTo() : xpos.getFrom();
					int yTo = allowSpanning ? ypos.getTo() : ypos.getFrom();
					storeCoordinates1(diagramElement, xpos.getFrom(), xTo, ypos.getFrom(), yTo);
				} else {
					overlaps.add(diagramElement);
					
				}
			}
		}
		
		// add remaining/dummy elements elements, by adding extra rows if need be.
		int xr = 0;
		while ((overlaps.size() > 0) || (xr < out.size())) {
			// add another (empty) column
			if (xr == out.size()) {
				DiagramElement[] ys = new DiagramElement[size.height];
				out.add(ys);
				size = new Dimension(size.width+1, size.height);
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
					storeCoordinates1(ys[y], xr, xr, y, y);
				}
			}
		
			xr++;
		}
		
		DiagramElement[][] done = (DiagramElement[][]) out.toArray(new DiagramElement[out.size()][]);
		
		for (DiagramElement de : ord.getContents()) {
			scaleCoordinates(de, size);
		}
		
		
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

	
	private void storeCoordinates1(DiagramElement diagramElement, int sx, int ex, int sy, int ey) {
		xPositions.put(diagramElement, new OPair<BigFraction>(BigFraction.getReducedFraction(sx, 1), BigFraction.getReducedFraction(ex+1, 1)));
		yPositions.put(diagramElement, new OPair<BigFraction>(BigFraction.getReducedFraction(sy, 1), BigFraction.getReducedFraction(ey+1, 1)));
	}
	
	private void scaleCoordinates(DiagramElement de, Dimension size) {
		OPair<BigFraction> xin = xPositions.get(de);
		OPair<BigFraction> yin = yPositions.get(de);
		xin = new OPair<BigFraction>(BigFraction.getReducedFraction(xin.getA().intValue(), size.width), BigFraction.getReducedFraction(xin.getB().intValue(), size.width));
		yin = new OPair<BigFraction>(BigFraction.getReducedFraction(yin.getA().intValue(), size.height), BigFraction.getReducedFraction(yin.getB().intValue(), size.height));
		xPositions.put(de, xin);
		yPositions.put(de, yin);
	}


	/**
	 * Iterates over the grid squares occupied by the ranges and either checks that they are empty, 
	 * or sets their value.
	 * @param allowSpanning 
	 */
	private static boolean ensureGrid(List<DiagramElement[]> out, IntegerRangeValue xpos, IntegerRangeValue ypos, DiagramElement in, boolean allowSpanning) {
		// ensure grid is large enough for the elements.
		int xTo = allowSpanning ? xpos.getTo()+1 : xpos.getFrom()+1;
		for (int x = xpos.getFrom(); x < xTo; x++) {
			DiagramElement[] ys = out.get(x);
			
			int yTo = allowSpanning ? ypos.getTo()+1: ypos.getFrom() + 1;
			for (int y = ypos.getFrom(); y < yTo; y++) {
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
