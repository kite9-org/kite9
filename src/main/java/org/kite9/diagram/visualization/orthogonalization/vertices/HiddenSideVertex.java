package org.kite9.diagram.visualization.orthogonalization.vertices;

import org.kite9.diagram.common.elements.DirectionEnforcingElement;
import org.kite9.diagram.common.elements.vertex.SideVertex;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;

/**
 * This is used in the special case of non-visible edges arriving at the vertex.
 * 
 * @author robmoffat
 *
 */
@Deprecated
class HiddenSideVertex extends SideVertex implements DirectionEnforcingElement {

	public HiddenSideVertex(String name, Connected cd, Connection cn) {
		super(name, cd, cn);
	}


}
