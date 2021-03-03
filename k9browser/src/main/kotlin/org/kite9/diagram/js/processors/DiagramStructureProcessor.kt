package org.kite9.diagram.js.processors

import org.kite9.diagram.dom.processors.AbstractInlineProcessor
import org.kite9.diagram.js.bridge.JSElementContext
import org.kite9.diagram.js.model.JSDiagramElementFactory
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.w3c.dom.Element

class DiagramStructureProcessor(val ef : JSDiagramElementFactory, val ctx: JSElementContext) : AbstractInlineProcessor() {

    var first : Diagram? = null

    override fun processTag(n: Element): Element {
        val parent = getParentDiagramElement(n.parentElement)
        val de = ef.createDiagramElement(n, parent)
        if (first == null) {
            if (de is Diagram) {
                first = de
            } else {
                throw UnsupportedOperationException("Top Element not diagram! " + de)
            }
        }

        if (de != null) {
            ctx.register(n,de);
            return n
        } else {
            return super.processTag(n)
        }
    }

    private fun getParentDiagramElement(n : Element?) : DiagramElement? {
        if (n == null) {
            return null
        }

        if (ctx.getRegisteredDiagramElement(n) != null) {
            return ctx.getRegisteredDiagramElement(n)
        }

        return getParentDiagramElement(n.parentElement)
    }
}