package org.kite9.diagram.dom.processors.xpath

import org.w3c.dom.Element

interface ValueReplacer {

    open fun performValueReplace(input: String, at: Element): String

}