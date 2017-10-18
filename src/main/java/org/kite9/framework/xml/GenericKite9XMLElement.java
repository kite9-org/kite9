package org.kite9.framework.xml;

import org.kite9.diagram.batik.templater.BasicTemplater;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * In future, this will be used for returning all diagram elements.
 * 
 * @author robmoffat
 *
 */
public class GenericKite9XMLElement extends AbstractStyleableXMLElement {

	public GenericKite9XMLElement(String name, ADLDocument owner) {
		super(name, owner);
	}

	@Override
	protected Node newNode() {
		return new GenericKite9XMLElement(getNodeName(), getOwnerDocument());
	}

	@Override
	public String toString() {
		return tagName;
	}
	
	public String getNodeName() {
		return tagName;
	}

}
