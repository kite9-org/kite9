package org.kite9.diagram.adl;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.framework.dom.elements.ADLDocument;
import org.kite9.framework.dom.elements.AbstractStyleableXMLElement;
import org.w3c.dom.Node;

public class CompositionalShape extends AbstractStyleableXMLElement  {

	private static final long serialVersionUID = 5343674853338333434L;
	
	public CompositionalShape(String id, ADLDocument doc) {
		super(id, "comp-shape", doc);
	}
	
	public CompositionalShape() {
		this.tagName = "comp-shape";
	}

	public int compareTo(DiagramElement arg0) {
		return 0;
	}

	@Override
	protected Node newNode() {
		return new CompositionalShape();
	}

	public RenderingInformation getRenderingInformation() {
		// TODO Auto-generated method stub
		return null;
	}

}
