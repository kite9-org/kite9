package org.kite9.diagram.common.elements.edge;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.algorithms.det.Deterministic;
import org.kite9.diagram.common.elements.ArtificialElement;
import org.kite9.diagram.common.elements.vertex.Vertex;

/**
 * Edges can represent parts of container borders or connections within the planarization of 
 * the diagram.  They connect from Vertex objects at each end.  
 * 
 * @author robmoffat
 *
 */
public interface Edge extends ArtificialElement, BiDirectional<Vertex>, Deterministic {
	
	public void setFrom(Vertex v);

	public void setTo(Vertex v);
		
	/**
	 * Unlinks the edge from the from, to vertices it is connected to.
	 */
	public void remove();
	
}
