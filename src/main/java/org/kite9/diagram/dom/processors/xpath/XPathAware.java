package org.kite9.diagram.dom.processors.xpath;

import java.util.Collection;

/**
 * Marker interface to say that some element of the xpath hierarchy 
 * can provide variables.
 * 
 * @author robmoffat
 *
 */
public interface XPathAware {

	public String getXPathVariable(String name);
	
	public Collection<String> getXPathVariables();

}
