/**
 * 
 */
package org.kite9.diagram.visualization.planarization.mapping;

import java.util.ArrayList;
import java.util.Deque;

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
	 * Creates or returns a middle vertex.
	 * 
	 * @param x A value between 0 and 1000.
	 * @param y A value between 0 and 1000
	 */
	public ContainerVertex createVertex(int x, int y);
}