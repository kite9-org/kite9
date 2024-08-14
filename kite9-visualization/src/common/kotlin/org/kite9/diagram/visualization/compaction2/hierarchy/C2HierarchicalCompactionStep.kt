package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.AbstractC2ContainerCompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.sets.SlideableSet
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

class C2HierarchicalCompactionStep(cd: CompleteDisplayer, r: GroupResult) : AbstractC2ContainerCompactionStep(cd, r) {

    var first = true;
    override fun compact(c: C2Compaction, g: Group) {
        if (!first) {
            return
        }

        first = true

        // first, collect all groups and order
        val allGroups = collectGroups(g).sortedBy { it.height }.distinctBy { it.groupNumber }
        allGroups.forEach { processGroup(it, c) }

        ensureCrossingPoints(c)
    }

    private fun ensureCrossingPoints(c: C2Compaction) {
        val sov = c.getSlackOptimisation(Dimension.V)
        val soh = c.getSlackOptimisation(Dimension.H)
        allContainers.forEach {
            val ssv = sov.getSlideablesFor(it)!!
            val ssh = soh.getSlideablesFor(it)!!

            val ssvc = sov.getContainer(ssv)
            val sshc = soh.getContainer(ssh)

//            c.setupContainerBlockers(ssvc, sshc)
//            c.createRoutableJunctions(sshc, ssvc)
        }
    }

    private fun processGroup(
        g: Group,
        c: C2Compaction
    ) {
        if (g is CompoundGroup) {
            if (horizontalAxis(g)) {
                mergeForAxis(c, g, Dimension.H, Layout.RIGHT, Layout.LEFT)
            }

            if (verticalAxis(g)) {
                mergeForAxis(c, g, Dimension.V, Layout.DOWN, Layout.UP)
            }

            if (combiningAxis(g)) {
                // in this case, we need to store whatever is available.
                val slackOptimisationV = c.getSlackOptimisation(Dimension.V)
                val va = slackOptimisationV.getSlideablesFor(g.a)
                val vb = slackOptimisationV.getSlideablesFor(g.b)

                val slackOptimisationH = c.getSlackOptimisation(Dimension.H)
                val ha = slackOptimisationH.getSlideablesFor(g.a)
                val hb = slackOptimisationH.getSlideablesFor(g.b)
                slackOptimisationV.add(g, va ?: vb!!)
                slackOptimisationH.add(g, ha ?: hb!!)
            }
        }

        if (horizontalAxis(g)) {
            completeContainers(c, g, Dimension.H)
        }

        if (verticalAxis(g)) {
            completeContainers(c, g, Dimension.V)
        }
    }

    private fun collectGroups(g: Group) : List<Group> {
        return when (g) {
            is CompoundGroup -> listOf(g) + collectGroups(g.a) + collectGroups(g.b)
            is LeafGroup -> listOf(g)
            else -> throw LogicException("wtf kotlin")
        }
    }


    private fun mergeForAxis(c: C2Compaction, g: CompoundGroup, d: Dimension, l1: Layout, l2: Layout) {
        val a = g.a
        val b = g.b

        val so = c.getSlackOptimisation(d)
        val ha = so.getSlideablesFor(a)
        val hb = so.getSlideablesFor(b)

        val hm = if ((ha == null) || (hb == null)) {
            ha ?: hb
        } else {
            val rl = getRequiredLayout(g, d)
            when (rl) {
                l1 -> {
                    separateRectangular(ha, hb, so, d)
                    ha.mergeWithGutter(hb, so)
                }

                l2 -> {
                    separateRectangular(hb, ha, so, d)
                    hb.mergeWithGutter(ha, so)
                }

                null -> {
                    hb.mergeWithOverlap(ha, so)
                }

                else -> {
                    throw LogicException("Shouldn't get an $d merge of $rl for group $g")
                }
            }
        }

        so.remove(a)
        so.remove(b)

        if (hm != null) {
            so.add(g, hm)
        }
        so.checkConsistency()
    }

    private fun getRequiredLayout(g: CompoundGroup, d: Dimension) : Layout? {
        return if (!g.axis.isLayoutRequired) {
            return null
        } else if (d==Dimension.H) {
            when (g.layout) {
                Layout.RIGHT, Layout.LEFT -> g.layout
                else -> null
            }
        } else {
            when (g.layout) {
                Layout.UP, Layout.DOWN -> g.layout
                else -> null
            }
        }
    }

    private fun separateRectangular(
        a: SlideableSet<*>,
        b: SlideableSet<*>,
        cso: C2SlackOptimisation,
        d: Dimension
    ) {
        val aSlideables =  cso.getRectangularsOnSide(Side.END,a)
        val bSlideables = cso.getRectangularsOnSide(Side.START, b)

        val aElements = aSlideables
            .flatMap { r -> r.anchors }
            .map { it.e }

        val bElements = bSlideables
            .flatMap { r -> r.anchors }
            .map { it.e }

        val distance = aElements.maxOfOrNull { ae ->
            bElements.maxOf { be -> getMinimumDistanceBetween(ae, Side.END, be, Side.START, d, null, true) }
        } ?: 0.0

        aSlideables.forEach { aS ->
            bSlideables.forEach { bS ->
                cso.ensureMinimumDistance(aS, bS, distance.toInt())
            }
        }
    }
    override val prefix: String
        get() = "HIER"

    override val isLoggingEnabled: Boolean
        get() = true
}