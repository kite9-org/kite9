/**
 * 
 */
package org.kite9.diagram.visualization.planarization.mapping;

import java.util.Collection;

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
	 * Unordered collection of vertices around the container.
	 */
	public Collection<ContainerVertex> getPerimeterVertices();
	
	/**
	 * Creates or returns a vertex from within the rectangle of the container.
	 * 
	 */
	public ContainerVertex createVertex(BigFraction x, BigFraction y);
	
	/**
	 * Returns all vertices in the container.
	 * @return
	 */
	public Collection<ContainerVertex> getAllVertices();
}