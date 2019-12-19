package org.kite9.diagram.dom.processors.xpath;

import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathResult;

/**
 * Provides a context from which xpath expressions can be resolved.
 * 
 * @author robmoffat
 *
 */
public interface ValueReplacer {
	
	public String getReplacementStringValue(String in);	
	
	public XPathResult getReplacementXML(String in, short type);
	
	public Node getLocation();
	
}