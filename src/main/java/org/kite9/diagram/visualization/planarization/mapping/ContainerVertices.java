/**
 * 
 */
package org.kite9.diagram.visualization.planarization.mapping;

import java.util.Collection;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

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
	public void identifyPerimeterVertices(RoutableHandler2D rh);
	
	public Collection<ContainerVertex> getPerimeterVertices();
	
	/**
	 * Creates or returns a vertex from within the rectangle of the container.
	 * 
	 */
	public ContainerVertex createVertex(BigFraction x, BigFraction y);
	
	/**
	 * Returns all vertices in the container, and in any parent containers (if a gridded container).
	 */
	public Collection<ContainerVertex> getAllAscendentVertices();
	
	/**
	 * Returns all vertices in the container, and in any child containers (if a gridded container).
	 */
	public Collection<ContainerVertex> getAllDescendentVertices();
	
	/**
	 * Returns vertices uniquely declared by this later of the container vertices.
	 */
	public Collection<ContainerVertex> getVerticesAtThisLevel();
	
	
	/**
	 * Looks at the hierarchy of container vertices, and merges any that overlap before 
	 * they are added to the planarization.
	 * 
	 * Returns null if there is already a vertex occupying the same place/position.
	 * 
	 * This is potentially a costly operation, as we have to check every vertex.
	 * 
	 */
	public ContainerVertex mergeDuplicates(ContainerVertex cv, RoutableHandler2D rh);
}