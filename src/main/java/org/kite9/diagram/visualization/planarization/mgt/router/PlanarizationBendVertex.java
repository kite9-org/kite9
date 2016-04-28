package org.kite9.diagram.visualization.planarization.mgt.router;

import org.kite9.diagram.common.elements.AbstractVertex;
import org.kite9.diagram.primitives.DiagramElement;

/**
 * This is used where a straight connection goes has to bend in the planarization to cross an edge in the same axis 
 * as itself
 * 
 * @author robmoffat
 */
public class PlanarizationBendVertex extends AbstractVertex {

	public PlanarizationBendVertex(String name) {
		super(name);
	}

	public DiagramElement getOriginalUnderlying() {
		return null;
	}
}