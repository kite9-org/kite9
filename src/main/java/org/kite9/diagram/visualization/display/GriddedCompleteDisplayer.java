package org.kite9.diagram.visualization.display;

import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;


/**
 * This class decorates a CompleteDisplayer by ensuring that all contents are correctly sized onto 
 * a grid.  To ensure that each diagram element has a middle, grid units returned are even for attr 
 * requiring a grid line intersecting their middle.  These attr are glyphs, arrows, keys, contexts
 * 
 * @author robmoffat
 *
 */
public class GriddedCompleteDisplayer implements CompleteDisplayer, Logable {

	protected Kite9Log log = new Kite9Log(this);

	CompleteDisplayer ded;
	double gridSize;
		
	private static final Map<Class<? extends DiagramElement>, Boolean> needsDoubleGrid = new HashMap<Class<? extends DiagramElement>, Boolean>();
	
	static {
	/*	needsDoubleGrid.put(Arrow.class, Boolean.TRUE);
		needsDoubleGrid.put(Link.class, Boolean.FALSE);
		needsDoubleGrid.put(Glyph.class, Boolean.TRUE);
		needsDoubleGrid.put(Context.class, Boolean.TRUE);
		needsDoubleGrid.put(Key.class, Boolean.FALSE);
		needsDoubleGrid.put(Diagram.class, Boolean.TRUE); */
	}
	
	private double snap(double width, DiagramElement de) {
		int snapWidths = isDoubleGrid(de) ? 2 : 1;
		return snap(width, snapWidths);
	}

	private double snap(double width, int snapWidths) {
		double count = Math.ceil(width / (gridSize*snapWidths));
		return count*snapWidths * gridSize;
	}
	
	private boolean isDoubleGrid(DiagramElement de) {
		System.out.println("Double grid needs fixing");
		if (de==null)
			return false;
		Class<?> c = de.getClass();
		Boolean result = needsDoubleGrid.get(c);
		return (Boolean.TRUE==result);
	}
	
	public GriddedCompleteDisplayer(CompleteDisplayer com) {
		this(com, 12);
	}

	public GriddedCompleteDisplayer(CompleteDisplayer com, int gridSize) {
		ded = com;
		this.gridSize = gridSize;
	}
	
	
	public void draw(DiagramElement element, RenderingInformation ri) {
		ded.draw(element, ri);
	}
	
	public CostedDimension size(DiagramElement element, Dimension2D within) {
		CostedDimension cd = ded.size(element, within == null ? null : new Dimension2D(within.getWidth(), within.getHeight()));
		if (cd==CostedDimension.NOT_DISPLAYABLE) {
			return cd;
		}
		
		CostedDimension out =  new CostedDimension(snap(cd.getWidth(), element), snap(cd.getHeight(), element), cd.cost);
		return out;
	}

	public String getPrefix() {
		return "GRID";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

	public boolean isVisibleElement(DiagramElement element) {
		return ded.isVisibleElement(element);
	}

	public boolean canDisplay(DiagramElement element) {
		return ded.canDisplay(element);
	}

	public double getMinimumDistanceBetween(DiagramElement a, Direction aSide, DiagramElement b, Direction bSide, Direction xy) {
		double minDist = ded.getMinimumDistanceBetween(a, aSide, b, bSide, xy);
		if (needsSnapping(a, aSide, b, bSide)) {
			minDist = snap(minDist, 1);		
			log.send(log.go() ? null : "Minimum snapped distances between " + a + "  " + aSide+ " "+ b + " "+ bSide +" in " + xy + " is " + minDist);
			return minDist;
		} else {
			return minDist;
		}
	}

	private boolean needsSnapping(DiagramElement a, Direction aSide, DiagramElement b, Direction bSide) {
		return true;
		//return (a!=b) || (a==null) || (b==null);
	}

	@Override
	public boolean isOutputting() {
		return ded.isOutputting();
	}

	@Override
	public void setOutputting(boolean outputting) {
		ded.setOutputting(outputting);
	}

	@Override
	public double getLinkPadding(DiagramElement element, Direction d) {
		return snap(ded.getLinkPadding(element, d),1);
	}
	
	@Override
	public double getPadding(DiagramElement element, Direction d) {
		return ded.getPadding(element, d);
	}

	@Override
	public double getTerminatorLength(Terminator terminator) {
		return ded.getTerminatorLength(terminator);
	}

	@Override
	public double getTerminatorReserved(Terminator terminator, Connection c) {
		return ded.getTerminatorReserved(terminator, c);
	}

	@Override
	public boolean requiresDimension(DiagramElement de) {
		return ded.requiresDimension(de);
	}

	@Override
	public double getLinkGutter(DiagramElement element, Direction d) {
		return snap(ded.getLinkGutter(element, d),1);
	}
}