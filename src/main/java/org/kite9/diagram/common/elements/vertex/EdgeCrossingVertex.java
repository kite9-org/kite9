package org.kite9.diagram.common.elements.vertex;

import java.util.HashSet;
import java.util.Set;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.common.Kite9ProcessingException;

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
	
	public EdgeCrossingVertex(String name, DiagramElement underlying1) {
		super(name);
		this.underlyings.add(underlying1);
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

	@Override
	public DiagramElement getOriginalUnderlying() {
		throw new Kite9ProcessingException("No underlying for this");
	}
	
}