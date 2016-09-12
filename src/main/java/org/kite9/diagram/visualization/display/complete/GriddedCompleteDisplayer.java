package org.kite9.diagram.visualization.display.complete;

import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Terminator;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.visualization.format.GraphicsSourceRenderer;
import org.kite9.framework.logging.Logable;


/**
 * This class decorates a CompleteDisplayer by ensuring that all contents are correctly sized onto 
 * a grid.  To ensure that each diagram element has a middle, grid units returned are even for attr 
 * requiring a grid line intersecting their middle.  These attr are glyphs, arrows, keys, contexts
 * 
 * @author robmoffat
 *
 */
public class GriddedCompleteDisplayer implements RequiresGraphicsSourceRendererCompleteDisplayer, Logable {

	RequiresGraphicsSourceRendererCompleteDisplayer ded;
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
	
	public GriddedCompleteDisplayer(RequiresGraphicsSourceRendererCompleteDisplayer com) {
		this(com, 12);
	}

	public GriddedCompleteDisplayer(RequiresGraphicsSourceRendererCompleteDisplayer com, int gridSize) {
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
		return false;
	}

	public boolean isVisibleElement(DiagramElement element) {
		return ded.isVisibleElement(element);
	}

	public void finish() {
		ded.finish();
	}

	public void initialize(GraphicsSourceRenderer<?> r, Dimension2D diagramSize) {
		Dimension2D gridded = new Dimension2D(diagramSize.x(), diagramSize.y());
		ded.initialize(r, gridded);
	}

	public boolean canDisplay(DiagramElement element) {
		return ded.canDisplay(element);
	}

	public double getMinimumDistanceBetween(DiagramElement a, Direction aSide, DiagramElement b, Direction bSide, Direction xy) {
		return snap(ded.getMinimumDistanceBetween(a, aSide, b, bSide, xy),1);
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
	public double getLinkMargin(DiagramElement element, Direction d) {
		return snap(ded.getLinkMargin(element, d),1);
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
