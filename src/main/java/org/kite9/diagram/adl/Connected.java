package org.kite9.diagram.adl;

import java.util.Collection;

import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.style.ContainerPosition;

/**
 * A diagram element which is consumes a rectangular area of space, and 
 * potentially has {@link Connection}s that link to other {@link Connected} items within the diagram.
 * 
 * @author robmoffat
 *
 */
public interface Connected extends DiagramElement {

	/**
	 * Returns an unmodifiable collection of links
	 */
	Collection<Connection> getLinks();
	
	/**
	 * Means that there exists a connection with this object at one end and c
	 * at the other.
	 */
	boolean isConnectedDirectlyTo(Connected c);
	
	/**
	 * Returns the connection between this object and c.
	 */
	Connection getConnectionTo(Connected c);
	
	/**
	 * Overrides the main one, since all Connecteds are areas on the diagram rather than links.
	 */
	RectangleRenderingInformation getRenderingInformation();
	
	/**
	 * Returns the container that this Connected is in.
	 */
	Container getContainer();
	
	/**
	 * Any other details about how this Connected is to be positioned in the container.
	 */
	ContainerPosition getContainerPosition();
}

