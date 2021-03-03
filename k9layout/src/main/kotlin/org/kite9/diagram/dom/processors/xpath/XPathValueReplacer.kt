package org.kite9.diagram.dom.processors.xpath

import org.kite9.diagram.dom.bridge.ElementContext
import org.w3c.dom.Element

class XPathValueReplacer(val ctx: ElementContext) : PatternValueReplacer() {

    override fun getReplacementStringValue(s: String, at: Element): String {
        val av = replaceVariables(s, at)
        val result = evaluateXPath(av, at)
        return result ?: ""
    }


    fun replaceVariables(s: String, at: Element) : String {
        val p = Regex("\\$[a-z]+")
        val out = replacePattern(p, s, at) { mr, at -> getVariableValue(mr.groupValues[0].substring(1), at) }
        return out
    }

    fun getVariableValue(v: String, at: Element) : String {
        var at : Element? = at;
        do {
            if (at is Element) {
                val de = ctx.getRegisteredDiagramElement(at)
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