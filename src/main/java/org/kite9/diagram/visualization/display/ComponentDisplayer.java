package org.kite9.diagram.visualization.display;

import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.visualization.format.pos.PositionInfoRenderer;

/**
 * Defines a pluggable framework for visualizing diagram attr, given a
 * width-constrained drawing system, and for getting the sizes of the diagrams
 * given these constraints.
 * 
 * @author robmoffat
 *
 */
public interface ComponentDisplayer {
	
	public void draw(DiagramElement element, RenderingInformation ri);
	
	/**
	 * If within is not provided, this returns the default size for an element.
	 * If within is provided, it attempts to fix the element within the size given.
	 * If the size given is too small, then a costed dimension will be returned with a
	 * positive cost.  Width constraints can be exceeded when a too-long word is added
	 * (hyphenation is not performed).  Height constraints can be exceeded when too 
	 * many rows of text are required.
	 */
	public CostedDimension size(DiagramElement element, Dimension2D within);

	public boolean isVisibleElement(DiagramElement element);
	
	public boolean canDisplay(DiagramElement element);
	
	public boolean isOutputting();

	/**
	 * Allows you to turn off the actual graphical output from this component,
	 * though size method and rendering information will still be set.
	 * 
	 * @See {@link PositionInfoRenderer}
	 */
	public void setOutputting(boolean outputting);
	
	/**
	 * Link Margins are the distance between a link and the side of the diagram element.
	 */
	public double getLinkMargin(DiagramElement element, Direction d);
		
	/**
	 * Given an area, returns the amount of padding that will be needed around it.
	 */
	public double getPadding(DiagramElement element, Direction d);
	
	/**
	 * Returns true if this element needs to have width and height in the diagram
	 */
	public boolean requiresDimension(DiagramElement de);
}
