package org.kite9.diagram.model;

import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.style.ContainerPosition;

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
	 * Returns the container that this rectangular is in.
	 */
	Container getContainer();
	
	/**
	 * Any other details about how this rectangular is to be positioned in the container.
	 */
	ContainerPosition getContainerPosition();
}
