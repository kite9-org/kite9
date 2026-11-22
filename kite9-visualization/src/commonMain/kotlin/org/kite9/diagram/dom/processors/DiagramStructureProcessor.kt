package org.kite9.diagram.dom.processors

import org.kite9.diagram.common.elements.factory.DiagramElementFactory
import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.w3c.dom.Element

class DiagramStructureProcessor(val ef : DiagramElementFactory<Element>, val ctx: ElementContext) : AbstractInlineProcessor() {

    var diagrams = mutableListOf<Diagram>()

    override fun processTag(n: Element): Element {
        val parent = getParentDiagramElement(n.parentNode as? Element)
        val de = ef.createDiagramElement(n, parent)
        if (de is Diagram) {
            diagrams.add(de)
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

        return getParentDiagramElement(n.parentNode as? Element)
    }
}