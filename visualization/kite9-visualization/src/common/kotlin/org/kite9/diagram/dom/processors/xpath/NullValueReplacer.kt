package org.kite9.diagram.dom.processors.xpath

import org.w3c.dom.Element

class NullValueReplacer : ValueReplacer {

    override fun performValueReplace(input: String, at: Element): String {
        return input
    }
}