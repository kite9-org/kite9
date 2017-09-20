package org.kite9.diagram.common.elements.vertex;

import java.util.Collections;
import java.util.Set;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.HPos;
import org.kite9.diagram.model.position.VPos;

/**
 * Used to model any old corner within the diagram. e.g. bend in a connection, corner of a vertex etc.
 */
public class SingleCornerVertex extends AbstractAnchoringVertex implements SingleElementVertex {

	private Anchor a;
		
	public SingleCornerVertex(String name, HPos lr, VPos ud, DiagramElement underlying) {
		super(name);
		this.a = new Anchor(ud, lr, underlying);
	}

	
	public HPos getLr() {
		return a.getLr();
	}


	public VPos getUd() {
		return a.getUd();
	}

	public DiagramElement getOriginalUnderlying() {
		return a.getDe();
	}


	@Override
	public void setX(double x) {
		super.setX(x);
		a.setX(x);
	}


	@Override
	public void setY(double y) {
		super.setY(y);
		a.setY(y);
	}


	@Override
	public boolean isPartOf(DiagramElement de) {
		return a.getDe() == de;
	}


	@Override
	public Set<DiagramElement> getDiagramElements() {
		return Collections.singleton(a.getDe());
	}
	
	
}
