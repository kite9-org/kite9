package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Container
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

class C2ContainmentCompactionStep(cd: CompleteDisplayer, r: GroupResult) : AbstractC2CompactionStep(cd) {

    private val containerCompletion: MutableMap<Group, MutableList<Container>> = mutableMapOf()

    override fun compact(c: C2Compaction, g: Group) {
        val completedContainers = containerCompletion[g]
        val sov = c.getSlackOptimisation(Dimension.V)
        var ssv : SlideableSet<*> = sov.getSlideablesFor(g)!!

        val soh = c.getSlackOptimisation(Dimension.H)
        var ssh : SlideableSet<*> = soh.getSlideablesFor(g)!!

        completedContainers?.forEach { container ->
            val csv = sov.getSlideablesFor(container)!!
            val csh = soh.getSlideablesFor(container)!!
            ssv = embed(sov, csv, ssv, Dimension.V)
            ssh = embed(soh, csh, ssh, Dimension.H)

            // replace the group slideable sets so we use these instead
            sov.add(g, ssv)
            soh.add(g, ssh)
        }
    }

    private fun embed(so: C2SlackOptimisation, outer: RectangularSlideableSet, inner: SlideableSet<*>, d: Dimension): RectangularSlideableSet {
        when (inner) {
            is RectangularSlideableSet -> embed(d, outer, inner, so)
            is RoutableSlideableSet -> {
                // first, ensure the buffer slideables are well-separated
                so.ensureMinimumDistance(outer.l, inner.bl,0)
                so.ensureMinimumDistance(inner.br, outer.r, 0)

                // now make sure that the rectangulars composing the routable are well-separated
                inner.getRectangularSlideableSets().forEach { embed(d, outer, it, so) }
            }
        }

        return outer
    }

    override val prefix: String
        get() = "CONS"

    override val isLoggingEnabled: Boolean
        get() = true


    init {
        r.containers().forEach { container ->
            val state = r.getStateFor(container)
            state?.contents?.forEach {
                val cc = containerCompletion.getOrPut(it) { mutableListOf() }
                cc.add(container)
            }
        }

        // ensure outer containers are handled later
        containerCompletion.values.forEach { c ->
            c.sortBy { -it.getDepth() }
        }
    }
}