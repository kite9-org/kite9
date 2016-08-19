package org.kite9.diagram.common.elements;

import org.kite9.diagram.common.Connected;

public class ConnectedVertex extends AbstractVertex {

	/**
	 * Connected vertices are ones representing glyphs and arrows in the diagram.
	 * They will have a width and height in the final diagram.
	 */
	@Override
	public boolean hasDimension() {
		return true;
	}

	Connected underlying;
	
	public ConnectedVertex(String id, Connected underlying) {
		super(id);
		this.underlying = underlying;
	}
	
	public Connected getOriginalUnderlying() {
		return underlying;
	}
	
}
