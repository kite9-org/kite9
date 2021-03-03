package org.kite9.diagram.dom.processors.post;

import org.kite9.diagram.dom.processors.xpath.PatternValueReplacer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathResult;

/**
 * Resolves replacement according to the current element.
 * 
 * @author robmoffat
 *
 */
public class DocumentValueReplacer extends PatternValueReplacer {
	
	Document d;

	public DocumentValueReplacer(Document d) {
		super();
		this.d = d;
	}

	@Override
	public String getReplacementStringValue(String xpath, Node at) {
		XPathResult out = (XPathResult) ((XPathEvaluator) d).evaluate(xpath, at, null, XPathResult.STRING_TYPE, null);
		return out.getStringValue();
	}

	@Override
	public XPathResult getReplacementXML(String xpath, short type, Node at) {
		XPathResult out = (XPathResult) ((XPathEvaluator) d).evaluate(xpath, at, null, type, null);
		return out;
	}

	@Override
	public Node getLocation() {
		return d;
	}
}
