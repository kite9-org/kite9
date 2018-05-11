package org.kite9.framework.dom.processors;

import org.kite9.framework.dom.elements.StyledKite9SVGElement;
import org.kite9.framework.dom.processors.ValueReplacingProcessor.ValueReplacer;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathNSResolver;
import org.w3c.dom.xpath.XPathResult;

public class XPathValueReplacer implements ValueReplacer {
	
    final XPathEvaluator xpathEvaluator;
    final StyledKite9SVGElement searchRoot;
    final XPathNSResolver resolver;
    
    public XPathValueReplacer(StyledKite9SVGElement in) {
    		this.searchRoot = in;
    		this.xpathEvaluator = in.getOwnerDocument();
    		this.resolver = null;
    }


	@Override
	public String getReplacementValue(String xpath) {
		return (String) xpathEvaluator.evaluate(xpath, searchRoot, resolver, XPathResult.STRING_TYPE, null);
	}

}
