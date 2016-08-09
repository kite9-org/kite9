package org.kite9.diagram.common.elements;

import org.kite9.diagram.adl.DiagramElement;



/**
 * This is used where two edges are required to cross each other.  This vertex is added at the crossing 
 * point to ensure that the planarization is 2d.
 * 
 * @author robmoffat
 */
public class EdgeCrossingVertex extends AbstractVertex {

	DiagramElement underlying = null;

	public EdgeCrossingVertex(String name) {
		super(name);
	}
	
	public EdgeCrossingVertex(String name, DiagramElement underlying) {
		super(name);
		this.underlying = underlying;
	}

	@Override
	public boolean hasDimension() {
		return false;
	}

	public DiagramElement getOriginalUnderlying() {
		return underlying;
	}

	
}