package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

class C2HierarchicalCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {
    override fun compact(c: C2Compaction, g: Group) {
        if (g is CompoundGroup) {
            compact(c, g.a)
            compact(c, g.b)

            val a = g.a
            val b = g.b
            val layout = g.layout

            val slackOptimisationH = c.getSlackOptimisation(Dimension.H)
            val slackOptimisationV = c.getSlackOptimisation(Dimension.V)

            val ha = slackOptimisationH.getSlideablesFor(a) as RoutableSlideableSet
            val va = slackOptimisationV.getSlideablesFor(a) as RoutableSlideableSet
            val hb = slackOptimisationH.getSlideablesFor(b) as RoutableSlideableSet
            val vb = slackOptimisationV.getSlideablesFor(b) as RoutableSlideableSet

            var vm : RoutableSlideableSet? = null
            var hm : RoutableSlideableSet? = null

            when(layout) {
                Layout.DOWN -> {
                    vm = va.merge(vb, slackOptimisationV)
                    hm = ha.mergeWithAxis(hb, slackOptimisationH)
                }
                Layout.UP -> {
                    vm = va.merge(vb, slackOptimisationV)
                    hm = hb.mergeWithAxis(ha, slackOptimisationH)
                }
                Layout.RIGHT -> {
                    vm = va.mergeWithAxis(vb, slackOptimisationV)
                    hm = hb.merge(ha, slackOptimisationH)
                }
                Layout.LEFT -> {
                    vm = vb.mergeWithAxis(va, slackOptimisationV)
                    hm = hb.merge(ha, slackOptimisationH)
                }
                else -> {
                    throw LogicException("Can't deal with this yet")
                }
            }

            slackOptimisationH.add(g, hm)
            slackOptimisationV.add(g, vm)

            slackOptimisationH.checkConsistency()
            slackOptimisationV.checkConsistency()
        }
    }


    override val prefix: String
        get() = "HIER"

    override val isLoggingEnabled: Boolean
        get() = true
}