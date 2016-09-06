package org.kite9.diagram.common.elements;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.algorithms.det.Deterministic;
import org.kite9.diagram.position.RenderingInformation;

/**
 * Edges can represent parts of container borders or connections within the planarization of 
 * the diagram.
 * 
 * @author robmoffat
 *
 */
public interface Edge extends ArtificialElement, BiDirectional<Vertex>, Deterministic {
	
	/**
	 * Done before inserting into planarization when to is lower vertex index than from.
	 */
	public void reverseDirection();
	
	/**
	 * Means that the edge goes from a higher vertex index to a lower one.
	 */
	public boolean isReversed();
		
	/**
	 * Unlinks the edge from the from, to vertices it is connected to.
	 */
	public void remove();
	
	/**
	 * Return details about the route this edge will take. 
	 */
	public RenderingInformation getRenderingInformation();
	
}
