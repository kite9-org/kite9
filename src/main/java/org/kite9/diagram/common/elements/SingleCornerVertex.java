package org.kite9.diagram.common.elements;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.VPos;

/**
 * Used to model any old corner within the diagram. e.g. bend in a connection, corner of a vertex etc.
 */
public class SingleCornerVertex extends AbstractAnchoringVertex {

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
	
	
}
