package org.kite9.diagram.visualization.compaction2.builders

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

/**
 * This turns the diagram's hierarchical structure of DiagramElement's into C2Slideables.
 *
 * @author robmoffat
 */
class C2ConnectedBuilderCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override val prefix: String
        get() = "SEGB"

    override val isLoggingEnabled: Boolean
        get() = true

    override fun compact(c: C2Compaction, g: Group) {
        if (g is LeafGroup) {
            checkCreate(c.getSlackOptimisation(Dimension.H), g.connected, Dimension.H, true)
            checkCreate(c.getSlackOptimisation(Dimension.H), g.container,  Dimension.H, true)
            checkCreate(c.getSlackOptimisation(Dimension.V), g.connected, Dimension.V, true)
            checkCreate(c.getSlackOptimisation(Dimension.V), g.container, Dimension.V, true)
        }
    }

    private fun checkCreate(cso: C2SlackOptimisation, de: DiagramElement?, d: Dimension, routing: Boolean) : SlideableSet? {
        if (de == null) {
            return null
        }

        if (de is Container) {
            checkCreateItems(cso, de, d, routing)
        }

        val ss = cso.getSlideablesFor(de);

        if (ss == null) {
            // we need to create these then
            val ms = getMinimumDistanceBetween(de, Side.START, de, Side.END, d, null, false)

            if (routing) {
                val bl = C2Slideable(cso, d, Purpose.GUTTER, de, Side.START)
                val l = C2Slideable(cso, d, Purpose.EDGE, de, Side.START)
                val c = C2Slideable(cso, d, Purpose.CENTER, de, Side.NEITHER)
                val r = C2Slideable(cso, d, Purpose.EDGE, de, Side.END)
                val br = C2Slideable(cso, d, Purpose.GUTTER, de, Side.END)
                cso.ensureMinimumDistance(l, r, ms.toInt())
                cso.ensureMinimumDistance(bl, l ,0 )
                cso.ensureMinimumDistance(l, c, (ms / 2.0).toInt())
                cso.ensureMinimumDistance(c, r, (ms / 2.0).toInt())
                cso.ensureMinimumDistance(r, br, 0)

                val out = RoutableSlideablesImpl(bl, l, c, r, br)
                cso.add(de, out)
                return out;
            } else {
                val l = C2Slideable(cso, d, Purpose.EDGE, de, Side.START)
                val r = C2Slideable(cso, d, Purpose.EDGE, de, Side.END)
                cso.ensureMinimumDistance(l, r, ms.toInt())
                val out = RectangularSlideablesImpl(l, r)
                cso.add(de, out)
                return out;
            }

        } else {
            return ss;
        }
    }

    private fun checkCreateItems(cso: C2SlackOptimisation, de: Container, d: Dimension, routing: Boolean) {
        de.getContents()
            .filterIsInstance<Rectangular>()
            .forEach { checkCreate(cso, it, d, routing) }
    }
}