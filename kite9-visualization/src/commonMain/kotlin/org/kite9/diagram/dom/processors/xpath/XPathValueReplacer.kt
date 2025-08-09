package org.kite9.diagram.dom.processors.xpath

import org.kite9.diagram.dom.bridge.ElementContext
import org.w3c.dom.Element

class XPathValueReplacer(val ctx: ElementContext) : PatternValueReplacer() {

    /**
     * This performs two types of replacement:
     * - If there is #{} contained in the string, it assumes each of their contents is an xpath
     * expression, needing to be replaced.
     * - If not, then it assumes the entire input is an xpath.
     */
    override fun performValueReplace(input: String, at: Element): String {
        if (input.contains(EMBEDDED_EXPRESSION)) {
            return performEmbeddedValueReplace(input, at)
        } else {
            return performCompleteValueReplace(input, at)
        }
    }

    private fun performEmbeddedValueReplace(input: String, at: Element): String {
        val done =
                replacePattern(EMBEDDED_EXPRESSION, input, at) { s, a ->
                    performCompleteValueReplace(s.groupValues[1], a)
                }

        return done
    }

    private fun performCompleteValueReplace(input: String, at: Element): String {
        val p = Regex("\\$([a-z\\-]+)")
        val done =
                replacePattern(p, input, at) { s, a ->
                    getReplacementStringValue(s.groupValues[1].lowercase(), a)
                }
        val result = if (done.trim().length > 0) evaluateXPath(done, at) else ""
        return result ?: ""
    }

    private fun isDocumentElement(at: Element): Boolean {
        return (at == at.ownerDocument?.documentElement)
    }

    override fun getReplacementStringValue(v: String, at: Element): String {
        var at: Element? = at
        do {
            if (at is Element) {
                val de =
                        if (isDocumentElement(at)) {
                            ctx.getDocumentReplacer(at)
                        } else {
                            ctx.getRegisteredDiagramElement(at)
                        }
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

    fun evaluateXPath(s: String, at: Element): String? {
        return ctx.evaluateXPath(s, at)
    }

    companion object {
        private val EMBEDDED_EXPRESSION = Regex("\\[\\[(.*?)\\]\\]") // matches [[thing]]
    }
}
