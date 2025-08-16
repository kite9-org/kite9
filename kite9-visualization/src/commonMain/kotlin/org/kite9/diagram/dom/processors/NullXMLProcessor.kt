package org.kite9.diagram.dom.processors

import org.w3c.dom.Node

class NullXMLProcessor : XMLProcessor {

    override fun processContents(from: Node?): Node? {
        return from
    }
}