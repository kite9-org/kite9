package org.kite9.diagram.common.elements;

import java.util.HashSet;
import java.util.Set;

import org.kite9.diagram.adl.DiagramElement;

/**
 * This is used where two edges are required to cross each other.  This vertex is added at the crossing 
 * point to ensure that the planarization is 2d.
 * 
 * @author robmoffat
 */
public class EdgeCrossingVertex extends AbstractVertex {

	Set<DiagramElement> underlyings = new HashSet<>();
	DiagramElement originalUnderlying;
	

	public EdgeCrossingVertex(String name) {
		super(name);
	}
	
	public EdgeCrossingVertex(String name, DiagramElement underlying) {
		super(name);
		this.underlyings.add(underlying);
		this.originalUnderlying = underlying;
	}

	@Override
	public boolean hasDimension() {
		return false;
	}

	public DiagramElement getOriginalUnderlying() {
		return originalUnderlying;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return underlyings.contains(de);
	}
	
	public void addUnderlying(DiagramElement de) {
		if (de != null) {
			underlyings.add(de);
		}
	}
	
}