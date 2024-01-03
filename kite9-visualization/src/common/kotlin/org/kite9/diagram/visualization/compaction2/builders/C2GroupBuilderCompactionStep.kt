package org.kite9.diagram.visualization.compaction2.builders

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.DiagramElement
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

    private fun checkCreate(cso: C2SlackOptimisation, g: Group, de: DiagramElement?, d: Dimension) : SlideableSet<*>? {
        if (de == null) {
            return null
        }

        val ss1 = cso.getSlideablesFor(g)

        if (ss1 != null) {
            return ss1
        }

        val ss2 = cso.getSlideablesFor(de)

        if (ss2 != null) {
            // we need to create these then
            val l = ss2.l
            val r = ss2.r
            val c = ss2.c
            val bl = C2OrbitSlideable(cso, d, setOf(de))
            val br = C2OrbitSlideable(cso, d, setOf(de))
            cso.ensureMinimumDistance(bl, l ,0 )
            cso.ensureMinimumDistance(r, br, 0)

            val out = RoutableSlideableSetImpl(setOf(ss2), setOfNotNull(bl, br, c), c, bl, br)
            cso.add(g, out)
            log.send("Created RoutableSlideableSetImpl for $de: ", out.getAll())
            return out

        } else {
            return null
        }
    }

}