package org.kite9.diagram.dom.model

import org.kite9.diagram.dom.bridge.ElementContext
import org.kite9.diagram.dom.painter.Painter
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.style.ContentTransform
import org.w3c.dom.Element

/**
 * This contains extra code relating to the Diagram itself, specifically, managing
 * the two-way referencing of links between diagram element.
 *
 * @author robmoffat
 */
class DiagramImpl(
    el: Element,
    ctx: ElementContext,
    rp: Painter,
    t: ContentTransform
) :
    ConnectedContainerImpl(el, null, ctx, rp, t), Diagram {

    private val connections: MutableList<Connection> = mutableListOf()

    override fun registerConnection(c: Connection) {
        connections.add(c)
    }

    override fun getConnectionsFor(c: Connected): Collection<Connection> {
        val out: MutableCollection<Connection> = ArrayList()
        for (co in connections) {
            if (co.getFrom() === c || co.getTo() === c) {
                out.add(co)
            }
        }
        return out
    }

    private val UNITS: Set<String> = setOf("pt", "cm", "em", "in", "ex", "px")

    /**
     * Because our ADLDocument knows about the CSSContext, we can resolve units in the xpath expressions.
     */
    override fun getXPathVariable(name: String): String? {
        if (UNITS.contains(name)) {
            return "" + ctx.getCssUnitSizeInPixels(name, theElement);
        }

        return super.getXPathVariable(name)
    }

}