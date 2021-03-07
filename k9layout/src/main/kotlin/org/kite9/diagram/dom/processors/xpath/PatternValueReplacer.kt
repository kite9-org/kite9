package org.kite9.diagram.dom.processors.xpath

import org.w3c.dom.Element

/**
 * Provides a context from which xpath expressions can be resolved.
 *
 * @author robmoffat
 */
abstract class PatternValueReplacer : ValueReplacer {

    abstract fun getReplacementStringValue(s: String, at: Element): String

    protected fun replacePattern(p: Regex, input: String, at: Element, replacer: (MatchResult, Element) -> String): String {
        var mr = p.find(input, 0)
        val out = StringBuilder()
        var place = 0

        while (mr != null) {
            val start = mr.range.start
            val end = mr.range.endInclusive

            out.append(input.substring(place, start))
            val replacement = replacer(mr, at)
            if (replacement.isEmpty()) {
                out.append("")
                //throw new Kite9XMLProcessingException("Couldn't determine value of '"+input+"' from "+at, at);
            }
            out.append(replacement.trim())
            place = end+1
            mr = mr.next()
        }
        out.append(input.substring(place))
        return out.toString()
    }
}