package org.kite9.diagram.adl;

import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.AbstractStyleableXMLElement;
import org.w3c.dom.Node;

public class LinkTerminator extends AbstractStyleableXMLElement {

	public LinkTerminator(String shape, boolean from) {
		this(shape, from, TESTING_DOCUMENT);
	}
	
	public LinkTerminator(String shape, boolean from, ADLDocument ownerDocument) {
		super(createID(), from ? "fromDecoration" : "toDecoration", ownerDocument);
		setAttribute("shape", shape);
	}

	@Override
	protected Node newNode() {
		return new LinkTerminator("", true);
	}

}
