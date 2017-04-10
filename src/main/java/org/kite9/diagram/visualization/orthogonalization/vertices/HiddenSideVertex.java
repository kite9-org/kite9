package org.kite9.diagram.visualization.orthogonalization.vertices;

import org.kite9.diagram.common.elements.DirectionEnforcingElement;
import org.kite9.diagram.common.elements.vertex.SideVertex;
import org.kite9.diagram.model.DiagramElement;

/**
 * This is used in the special case of non-visible edges arriving at the vertex.
 * 
 * @author robmoffat
 *
 */
class HiddenSideVertex extends SideVertex implements DirectionEnforcingElement {

	public HiddenSideVertex(String name, DiagramElement underlying) {
		super(name, underlying);
	}

}
