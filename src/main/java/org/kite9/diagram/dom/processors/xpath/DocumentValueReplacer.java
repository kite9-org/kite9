package org.kite9.diagram.dom.processors.xpath;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathResult;

public class DocumentValueReplacer implements ValueReplacer {
	
	Document d;

	public DocumentValueReplacer(Document d) {
		super();
		this.d = d;
	}

	@Override
	public String getReplacementStringValue(String xpath) {
		XPathResult out = (XPathResult) ((XPathEvaluator) d).evaluate(xpath, d, null, XPathResult.STRING_TYPE, null);
		return out.getStringValue();
	}

	@Override
	public XPathResult getReplacementXML(String xpath, short type) {
		XPathResult out = (XPathResult) ((XPathEvaluator) d).evaluate(xpath, d, null, type, null);
		return out;
	}

	@Override
	public Node getLocation() {
		return d;
	}
}
