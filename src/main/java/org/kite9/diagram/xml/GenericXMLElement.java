package org.kite9.diagram.xml;

import org.w3c.dom.Node;

/**
 * In future, this will be used for returning all diagram elements.
 * 
 * @author robmoffat
 *
 */
public class GenericXMLElement extends AbstractStyleableXMLElement {

	public GenericXMLElement(String name, ADLDocument owner) {
		super(name, owner);
	}

	@Override
	protected Node newNode() {
		return new GenericXMLElement(getNodeName(), getOwnerDocument());
	}

	@Override
	public String toString() {
		return tagName;
	}
	
	public String getNodeName() {
		return tagName;
	}
	
	

}
