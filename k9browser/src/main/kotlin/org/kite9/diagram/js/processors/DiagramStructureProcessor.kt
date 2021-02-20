package org.kite9.diagram.js.processors

import org.kite9.diagram.common.elements.factory.DiagramElementFactory
import org.kite9.diagram.dom.processors.AbstractInlineProcessor
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.w3c.dom.Element

class DiagramStructureProcessor(val ef : DiagramElementFactory<Element>) : AbstractInlineProcessor() {

    var stack = ArrayDeque<DiagramElement>()
    var first : Diagram? = null

    override fun processTag(n: Element): Element {
        val top = stack.lastOrNull()
        val de = ef.createDiagramElement(n, top)
        if (stack.isEmpty()) {
            if (de is Diagram) {
                first = de as Diagram;
            } else {
                throw UnsupportedOperationException("Top Element not diagram! " + de)
            }
        }

        if (de != null) {
            stack.addLast(de);
            val out = super.processTag(n)
            stack.removeLast()
            return out
        } else {
            return super.processTag(n)
        }
    }
}