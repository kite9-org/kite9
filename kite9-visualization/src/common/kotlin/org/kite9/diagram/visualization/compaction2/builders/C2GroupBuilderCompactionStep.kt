package org.kite9.diagram.visualization.compaction2.builders

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSetImpl
import org.kite9.diagram.visualization.compaction2.sets.SlideableSet
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
            val e = g.connected
            if (e is Rectangular) {
                checkCreate(c.getSlackOptimisation(Dimension.H), g, e, Dimension.H)
                checkCreate(c.getSlackOptimisation(Dimension.V), g, e, Dimension.V)
            } else {
                // leaf node must be for container arrival
                val f = g.container!!
                checkCreateIntersectionOnly(c.getSlackOptimisation(Dimension.H), g, f, Dimension.H)
                checkCreateIntersectionOnly(c.getSlackOptimisation(Dimension.V), g, f, Dimension.V)
            }
        }
    }

    private fun checkCreateIntersectionOnly(cso: C2SlackOptimisation, g: Group, c: Container, d: Dimension) : SlideableSet<*>? {
        val ss1 = cso.getSlideablesFor(g)

        if (ss1 != null) {
            return ss1
        }

        val ic = C2IntersectionSlideable(cso, d, listOf(c))
        val bl = C2OrbitSlideable(cso, d, setOf())
        val br = C2OrbitSlideable(cso, d, setOf())
        val out = RoutableSlideableSetImpl(ic, bl, br)

        cso.add(g, out)
        log.send("Created a RouteableSlideableSet for $c: ", out.getAll())
        return out
    }

    private fun checkCreate(cso: C2SlackOptimisation, g: Group, de: Rectangular, d: Dimension) : SlideableSet<*>? {
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
            val bl = C2OrbitSlideable(cso, d, setOf(RectAnchor(de, Side.START)))
            val br = C2OrbitSlideable(cso, d, setOf(RectAnchor(de, Side.END)))
            cso.ensureMinimumDistance(bl, l ,0 )
            cso.ensureMinimumDistance(r, br, 0)
            val out = RoutableSlideableSetImpl(setOfNotNull(bl, br, c), c, bl, br)
            cso.contains(out, ss2)
            cso.add(g, out)
            log.send("Created RoutableSlideableSetImpl for $de: ", out.getAll())
            return out

        } else {
            return null
        }
    }

}