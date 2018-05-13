package org.kite9.diagram.dom.processors.xpath;

import java.util.Map;

/**
 * Marker interface to say that some element of the xpath hierarchy 
 * can provide variables.
 * 
 * @author robmoffat
 *
 */
public interface XPathAware {

	public Map<String, String> getXPathVariables();

}
