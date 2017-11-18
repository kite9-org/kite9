package org.kite9.diagram.model;

import java.util.Collection;

import org.kite9.diagram.model.style.ConnectionsSeparation;

/**
 * A diagram element which is consumes a rectangular area of space, and 
 * potentially has {@link Connection}s that link to other {@link Connected} items within the diagram.
 * 
 * Unlike the label, it is therefore involved in the Planarization phase.
 * 
 * @author robmoffat
 *
 */
public interface Connected extends Rectangular {

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
	
	ConnectionsSeparation getConnectionsSeparationApproach();
}

