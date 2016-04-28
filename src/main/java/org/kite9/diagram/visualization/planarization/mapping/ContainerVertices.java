/**
 * 
 */
package org.kite9.diagram.visualization.planarization.mapping;

import java.util.LinkedList;

import org.kite9.diagram.position.Direction;

public interface ContainerVertices {
		
	/**
	 * Clockwise ordering of vertices around the container, from top left.
	 */
	public LinkedList<ContainerVertex> getVertices();
	
	/**
	 * Creates or returns a middle vertex for the side d.
	 */
	public ContainerVertex getCentralVertexOnSide(Direction d);
}