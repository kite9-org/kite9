package org.kite9.diagram.model;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.ContainerPosition;
import org.kite9.diagram.model.style.DiagramElementSizing;

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
	
	
	/**
	 * Returns the container that this rectangular is in.
	 */
	Container getContainer();
	
	/**
	 * Any other details about how this rectangular is to be positioned in the container.
	 */
	ContainerPosition getContainerPosition();
}
