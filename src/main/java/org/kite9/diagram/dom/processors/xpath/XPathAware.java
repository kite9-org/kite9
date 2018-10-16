package org.kite9.diagram.dom.processors.xpath;

/**
 * Marker interface to say that some element of the xpath hierarchy 
 * can provide variables.
 * 
 * @author robmoffat
 *
 */
public interface XPathAware {

	public String getXPathVariable(String name);

}
