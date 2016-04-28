package org.kite9.diagram.common.elements;

import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.AbstractBiDirectional;

public abstract class AbstractEdge extends AbstractBiDirectional<Vertex> implements Edge {

	private static final long serialVersionUID = -5546065591109486265L;
	
	/**
	 * Ensures the identity of the edge doesn't change when we alter one of it's endpoints
	 */
	private int hashCode;

	public void setDrawDirectionFrom(Vertex end, Direction d) {
	    if (d==null) {
		this.drawDirection = d;
	    }
	    
	    if (end==from) {
		this.drawDirection = d;
	    } else {
		this.drawDirection = Direction.reverse(d);
	    }
	    
	}

	public AbstractEdge(Vertex from, Vertex to, Direction drawDirection) {
		super(from, to, drawDirection);
		this.hashCode = from.hashCode() + to.hashCode();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public void setRenderingInformation(RenderingInformation ri) {
		this.renderingInformation = ri;
	}

}