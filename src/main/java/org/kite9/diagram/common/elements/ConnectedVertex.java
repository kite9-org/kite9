package org.kite9.diagram.common.elements;

import org.kite9.diagram.adl.Connected;

public class ConnectedVertex extends AbstractVertex {

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
