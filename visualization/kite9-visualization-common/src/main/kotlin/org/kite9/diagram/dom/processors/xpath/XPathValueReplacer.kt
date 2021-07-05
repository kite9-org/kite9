package org.kite9.diagram.dom.processors.xpath

import org.kite9.diagram.dom.bridge.ElementContext
import org.w3c.dom.Element


class XPathValueReplacer(val ctx: ElementContext) : PatternValueReplacer() {

    override fun performValueReplace(input: String, at: Element): String {
        val p = Regex("\\$([a-z\\-]+)")
        val done = replacePattern(p, input, at) { s, a -> getReplacementStringValue(s.groupValues[1].toLowerCase(), a) }
        val result = evaluateXPath(done, at)
        return result ?: ""
    }

    private fun isDocumentElement(at: Element) : Boolean {
        return (at == at.ownerDocument?.documentElement)
    }

    override fun getReplacementStringValue(v: String, at: Element) : String {
        var at : Element? = at;
        do {
            if (at is Element) {
                val de = if (isDocumentElement(at)) { ctx.getDocumentReplacer() }  else { ctx.getRegisteredDiagramElement(at) }
                if (de is XPathAware) {
                    val out = de.getXPathVariable(v)
                    if (out != null) {
                        return out
                    }
                }
            }
            if (at == null) {
                return ""
            } else {
                val parent = at.parentNode
                if (parent is Element) {
                    at = parent
                } else {
                    at = null
                }
            }
        } while (at != null)

        return ""
    }

    fun evaluateXPath(s: String, at: Element) : String? {
        return ctx.evaluateXPath(s, at)
    }



}