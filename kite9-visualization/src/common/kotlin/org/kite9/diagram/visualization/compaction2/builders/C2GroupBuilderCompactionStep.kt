package org.kite9.diagram.visualization.compaction2.builders

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

/**
 * This turns the diagram's hierarchical structure of DiagramElement's into C2Slideables.
 *
 * @author robmoffat
 */
class C2GroupBuilderCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override val prefix: String
        get() = "SEGB"

    override val isLoggingEnabled: Boolean
        get() = true

    override fun compact(c: C2Compaction, g: Group) {
        if (g is CompoundGroup) {
            compact(c, g.a)
            compact(c, g.b)
        } else if (g is LeafGroup) {
            checkCreate(c.getSlackOptimisation(Dimension.H), g, g.connected, Dimension.H)
            checkCreate(c.getSlackOptimisation(Dimension.V), g, g.connected, Dimension.V)
        }
    }

    private fun checkCreate(cso: C2SlackOptimisation, g: Group, de: DiagramElement?, d: Dimension) : SlideableSet? {
        if (de == null) {
            return null
        }

        val ss = cso.getSlideablesFor(de);

        if (ss == null) {
            // we need to create these then
            val ms = getMinimumDistanceBetween(de, Side.START, de, Side.END, d, null, false)

            val bl = C2Slideable(cso, d, Purpose.ROUTE, de, Side.START)
            val l = C2Slideable(cso, d, Purpose.EDGE, de, Side.START)
            val c = C2Slideable(cso, d, Purpose.ROUTE, de, Side.NEITHER)
            val r = C2Slideable(cso, d, Purpose.EDGE, de, Side.END)
            val br = C2Slideable(cso, d, Purpose.ROUTE, de, Side.END)
            cso.ensureMinimumDistance(l, r, ms.toInt())
            cso.ensureMinimumDistance(bl, l ,0 )
            cso.ensureMinimumDistance(l, c, (ms / 2.0).toInt())
            cso.ensureMinimumDistance(c, r, (ms / 2.0).toInt())
            cso.ensureMinimumDistance(r, br, 0)

            val out = RoutableSlideableSetImpl(listOf(r, l), listOf(bl, br, c), c, bl, br)
            cso.add(de, out)
            cso.add(g, out)
            log.send("Created RoutableSlideableSetImpl: ", out.getAll())
            return out;

        } else {
            return ss;
        }
    }

}