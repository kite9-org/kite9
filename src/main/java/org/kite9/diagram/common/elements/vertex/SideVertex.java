package org.kite9.diagram.common.elements.vertex;

import java.util.HashSet;
import java.util.Set;

import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.common.Kite9ProcessingException;

/**
 * A vertex modelling the join between a Connection and a Connected diagram element, created in the 
 * process of giving a vertex a dimensioned shape in orthogonalization.
 * 
 * @author robmoffat
 *
 */
public class SideVertex extends AbstractVertex implements MultiElementVertex {
	
	private Set<DiagramElement> underlyings = new HashSet<>();
	
	public SideVertex(String name, Connected cd, DiagramElement cn) {
		super(name);
		this.underlyings.add(cd);
		this.underlyings.add(cn);
	}

	@Override
	public Set<DiagramElement> getDiagramElements() {
		return underlyings;
	}

	@Override
	public boolean isPartOf(DiagramElement c) {
		return underlyings.contains(c);
	}

	@Override
	public DiagramElement getOriginalUnderlying() {
		throw new Kite9ProcessingException();
	}
}
