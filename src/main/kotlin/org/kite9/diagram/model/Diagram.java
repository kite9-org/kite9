package org.kite9.diagram.model;

import java.util.Collection;

/**
 * A diagram is basically a container which manages a number of connections between elements 
 * contained within it.
 * 
 * @author robmoffat
 *
 */
public interface Diagram extends Container {

	public Collection<Connection> getConnectionsFor(Connected c);
	
}