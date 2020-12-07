package org.kite9.diagram.common.elements.vertex;

import java.util.HashSet;
import java.util.Set;

import org.kite9.diagram.model.DiagramElement;

/**
 * This is used where two edges are required to cross each other.  This vertex is added at the crossing 
 * point to ensure that the planarization is 2d.
 * 
 * @author robmoffat
 */
public class EdgeCrossingVertex extends AbstractVertex implements MultiElementVertex {

	Set<DiagramElement> underlyings = new HashSet<>();	

	public EdgeCrossingVertex(String name) {
		super(name);
	}
	
	public EdgeCrossingVertex(String name, Set<DiagramElement> underlyings) {
		super(name);
		this.underlyings = underlyings;
	}

	@Override
	public boolean hasDimension() {
		return false;
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

	@Override
	public Set<DiagramElement> getDiagramElements() {
		return underlyings;
	}
}