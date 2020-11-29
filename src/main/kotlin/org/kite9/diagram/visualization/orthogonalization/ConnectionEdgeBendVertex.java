package org.kite9.diagram.visualization.orthogonalization;

import java.util.Collections;
import java.util.Set;

import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.vertex.AbstractVertex;
import org.kite9.diagram.common.elements.vertex.SingleElementVertex;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;

/**
 * Models a bend within an Edge.  Darts are horizontal or vertical, so this allows
 * for the modelling of the joins between darts in the Edge's path.
 * 
 * @author robmoffat
 *
 */
public class ConnectionEdgeBendVertex extends AbstractVertex implements SingleElementVertex {
	
	private Connection underlying;
	
	public ConnectionEdgeBendVertex(String name, ConnectionEdge underlying) {
		super(name);
		this.underlying = underlying.getOriginalUnderlying();
	}

	public DiagramElement getOriginalUnderlying() {
		return underlying;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return underlying == de;
	}

	@Override
	public Set<DiagramElement> getDiagramElements() {
		return Collections.singleton(underlying);
	}

}
