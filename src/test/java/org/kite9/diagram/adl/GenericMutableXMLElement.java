package org.kite9.diagram.adl;

import org.kite9.diagram.dom.elements.ADLDocument;
import org.w3c.dom.Node;

public class GenericMutableXMLElement extends AbstractMutableXMLElement {

	public GenericMutableXMLElement(String name, ADLDocument owner) {
		super(name, owner);
	}

	@Override
	protected Node newNode() {
		return new GenericMutableXMLElement(getNodeName(), getOwnerDocument());
	}

	@Override
	public String toString() {
		return tagName;
	}
	
	public String getNodeName() {
		return tagName;
	}


}
