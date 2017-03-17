package org.kite9.diagram.visualization.display;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RenderingInformation;

public interface Displayer {

	void draw(DiagramElement element, RenderingInformation ri);

	/**
	 * If within is not provided, this returns the *minimum* size for an element.
	 * If within is provided, it attempts to fix the element within the size given.
	 * If the size given is too small, then a costed dimension will be returned with a
	 * positive cost.  Width constraints can be exceeded when a too-long word is added
	 * (hyphenation is not performed).  Height constraints can be exceeded when too 
	 * many rows of text are required.
	 * 
	 * Note that size will *not* consider nested elements.  
	 */
	CostedDimension size(DiagramElement element, Dimension2D within);

	boolean isVisibleElement(DiagramElement element);

	boolean canDisplay(DiagramElement element);

	boolean isOutputting();

	/**
	 * Allows you to turn off the actual graphical output from this component,
	 * though size method and rendering information will still be set.
	 */
	void setOutputting(boolean outputting);

	/**
	 * Link Margins are the distance between a link and the side of the diagram element.
	 */
	double getLinkPadding(DiagramElement element, Direction d);

	/**
	 * Given an area, returns the amount of padding that will be needed around it.
	 */
	double getPadding(DiagramElement element, Direction d);

	/**
	 * Returns true if this element needs to have width and height in the diagram
	 */
	boolean requiresDimension(DiagramElement de);

}