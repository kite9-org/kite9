package org.kite9.diagram.adl;

import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.style.DiagramElementSizing;

/**
 * Marker interface for diagram elements which consume a rectangular space, and therefore
 * return {@link RectangleRenderingInformation}.
 * 
 * @author robmoffat
 *
 */
public interface Rectangular extends DiagramElement {

	public RectangleRenderingInformation getRenderingInformation();
	
	/**
	 * Margin is the minimum distance from this element to elements around it that it's not connected to.
	 */
	public double getMargin(Direction d);
	
	/**
	 * Padding is the space inside this element that is consumed over and above the space of the child
	 * elements.
	 */
	public double getPadding(Direction d);
	
	public DiagramElementSizing getSizing();
}
