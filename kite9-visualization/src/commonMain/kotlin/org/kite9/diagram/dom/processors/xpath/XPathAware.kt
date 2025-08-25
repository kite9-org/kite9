package org.kite9.diagram.dom.processors.xpath

/**
 * Marker interface to say that some element of the xpath hierarchy
 * can provide variables.
 *
 * @author robmoffat
 */
interface XPathAware {

    fun getXPathVariable(name: String): String?

}