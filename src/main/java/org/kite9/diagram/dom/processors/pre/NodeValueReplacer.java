package org.kite9.diagram.dom.processors.pre;

import org.kite9.diagram.dom.processors.xpath.ValueReplacer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathResult;

/**
 * Resolves replacement according to the template element.
 * 
 * @author robmoffat
 *
 */
public class NodeValueReplacer extends ValueReplacer {
	
	Node from;
	Document d;

	public NodeValueReplacer(Node from) {
		super();
		this.from = from;
		this.d = from.getOwnerDocument();
	}

	@Override
	public String getReplacementStringValue(String xpath, Node at) {
		XPathResult out = (XPathResult) ((XPathEvaluator) d).evaluate(xpath, from, null, XPathResult.STRING_TYPE, null);
		return out.getStringValue();
	}

	@Override
	public XPathResult getReplacementXML(String xpath, short type, Node at) {
		XPathResult out = (XPathResult) ((XPathEvaluator) d).evaluate(xpath, from, null, type, null);
		return out;
	}

	@Override
	public Node getLocation() {
		return from;
	}

	@Override
	public String toString() {
		return "NodeValueReplacer [from=" + from + "]";
	}
	
}
