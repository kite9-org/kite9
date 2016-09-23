package org.kite9.diagram.visualization.planarization.rhd.grid;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.Connected;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.framework.serialization.CSSConstants;
import org.kite9.framework.serialization.IntegerRangeValue;

/**
 * Tools for helping create Grid structure.  
 * Split out from {@link GroupPhase}.
 * 
 * @author robmoffat
 *
 */
public class GridHelp {


	
	private static Dimension calculateGridSize(Container ord) {
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
	
	
	public static DiagramElement[][] placeOnGrid(Container ord) {
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
				} else {
					overlaps.add(diagramElement);
				}
			}
		}
		
		// add remaining/dummy elements elements, by adding extra rows if need be.
		int xr = 0;
		while ((overlaps.size() > 0) && (xr < out.size())) {
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
					}
				}
			}
		
			xr++;
		}
		
		return (DiagramElement[][]) out.toArray(new DiagramElement[out.size()][]);
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
}
