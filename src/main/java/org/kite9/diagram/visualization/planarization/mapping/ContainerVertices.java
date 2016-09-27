/**
 * 
 */
package org.kite9.diagram.visualization.planarization.mapping;

import java.util.ArrayList;

import org.apache.commons.math.fraction.BigFraction;

/**
 * This automatically comes populated with the four corner vertices, but 
 * by using the <pre>createVertex</pre> method, you can add extra ones.
 * 
 * @author robmoffat
 *
 */
public interface ContainerVertices {
		
	/**
	 * Ordering of vertices around the container, from top left clockwise.
	 */
	public ArrayList<ContainerVertex> getPerimeterVertices();
	
	/**
	 * Creates or returns a vertex from within the rectangle of the container.
	 * 
	 */
	public ContainerVertex createVertex(BigFraction x, BigFraction y);
}