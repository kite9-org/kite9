package org.kite9.diagram.common.elements.edge;

import org.kite9.diagram.common.elements.AbstractBiDirectional;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.position.Direction;

public abstract class AbstractEdge extends AbstractBiDirectional<Vertex> implements Edge {
	
	/**
	 * Ensures the identity of the edge doesn't change when we alter one of it's endpoints
	 */
	private int hashCode;

	public AbstractEdge(Vertex from, Vertex to, Direction d) {
		super(from.getID()+"-"+to.getID(), from, to, d);
		this.hashCode = from.hashCode() + to.hashCode();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	

}