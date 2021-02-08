package org.kite9.diagram.dom.processors

import org.w3c.dom.Node

interface XMLProcessor {

    fun processContents(from: Node?): Node?
}