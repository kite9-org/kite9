package org.kite9.diagram.visualization.orthogonalization;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.AbstractVertex;
import org.kite9.diagram.common.elements.Edge;

/**
 * Models a bend within an Edge.  Darts are horizontal or vertical, so this allows
 * for the modelling of the joins between darts in the Edge's path.
 * 
 * @author robmoffat
 *
 */
public class EdgeBendVertex extends AbstractVertex {
	
	private Edge underlying;
	
	public EdgeBendVertex(String name, Edge underlying) {
		super(name);
		this.underlying = underlying;
	}

	public DiagramElement getOriginalUnderlying() {
		return underlying.getOriginalUnderlying();
	}

}
