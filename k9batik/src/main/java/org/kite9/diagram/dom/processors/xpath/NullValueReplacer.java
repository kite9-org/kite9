package org.kite9.diagram.dom.processors.xpath;

import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathResult;

public class NullValueReplacer extends ValueReplacer {

	@Override
	public String getReplacementStringValue(String in, Node at) {
		return null;
	}

	@Override
	public XPathResult getReplacementXML(String in, short type, Node at) {
		return null;
	}

	@Override
	public Node getLocation() {
		return null;
	}

	@Override
	public String performValueReplace(String input, Node at) {
		return null;
	}	
}
