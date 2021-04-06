package org.kite9.diagram.adl;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class BasicPort extends AbstractMutableXMLElement {

	private static final long serialVersionUID = 3578883565482903409L;

	@Override
	public String toString() {
		return "Port: "+getID();
	}

	public BasicPort(String id, Document doc) {
		super(id, "port", doc);
	}

	@Override
	protected Node newNode() {
		return new BasicPort(createID(), (Document) ownerDocument);
	}

	public String getXMLId() {
		return getAttribute("id");
	}
}
