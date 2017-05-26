package org.kite9.diagram.common.elements.vertex;

import java.util.Set;

import org.kite9.diagram.model.DiagramElement;

/**
 * A vertex modelling the join between a Connection and a Connected diagram element, created in the 
 * process of giving a vertex a dimensioned shape in orthogonalization.
 * 
 * @author robmoffat
 *
 */
public class DartJunctionVertex extends AbstractVertex implements MultiElementVertex {
	
	private Set<DiagramElement> underlyings;
	
	public DartJunctionVertex(String name, Set<DiagramElement> a) {
		super(name);
		this.underlyings = a;
	}

	@Override
	public Set<DiagramElement> getDiagramElements() {
		return underlyings;
	}

	@Override
	public boolean isPartOf(DiagramElement c) {
		return underlyings.contains(c);
	}
}
