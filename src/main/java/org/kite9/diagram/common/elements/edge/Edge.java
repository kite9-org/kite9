package org.kite9.diagram.common.elements.edge;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.algorithms.det.Deterministic;
import org.kite9.diagram.common.elements.ArtificialElement;
import org.kite9.diagram.common.elements.vertex.Vertex;

/**
 * Edges can represent parts of container borders or connections within the planarization and orthogonalization of 
 * the diagram.  They connect from Vertex objects at each end.  
 * 
 * @author robmoffat
 *
 */
public interface Edge extends ArtificialElement, BiDirectional<Vertex>, Deterministic {
	
}
