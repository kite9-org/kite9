package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis

class C2HierarchicalCompactionStep(cd: CompleteDisplayer, r: GroupResult) : AbstractC2ContainerCompactionStep(cd, r) {
    override fun compact(c: C2Compaction, g: Group) {
        if (g is CompoundGroup) {
            compact(c, g.a)
            compact(c, g.b)

            val a = g.a
            val b = g.b

            // horizontal axia
            val slackOptimisationH = c.getSlackOptimisation(Dimension.H)
            val ha = slackOptimisationH.getSlideablesFor(a)
            val hb = slackOptimisationH.getSlideablesFor(b)
            val hm = if ((ha == null) || (hb == null)) {
                ha ?: hb
            } else {
                when(getRequiredLayout(g, Dimension.H)) {
                    Layout.RIGHT, Layout.HORIZONTAL -> {
                        separateRectangular(ha, Side.END, hb, Side.START, slackOptimisationH, Dimension.H)
                        ha.mergeWithGutter(hb, slackOptimisationH)
                    }
                    Layout.LEFT -> {
                        separateRectangular(hb, Side.END, ha, Side.START, slackOptimisationH, Dimension.H)
                        hb.mergeWithGutter(ha, slackOptimisationH)
                    }
                    else -> {
                        hb.mergeWithOverlap(ha, slackOptimisationH)
                    }
                }
            }

            slackOptimisationH.remove(a)
            slackOptimisationH.remove(b)
            if (hm != null) {
                slackOptimisationH.add(g, hm)
            }
            slackOptimisationH.checkConsistency()


            // vertical axis
            val slackOptimisationV = c.getSlackOptimisation(Dimension.V)
            val va = slackOptimisationV.getSlideablesFor(a)
            val vb = slackOptimisationV.getSlideablesFor(b)
            val vm = if ((va == null) || (vb == null)) {
                va ?: vb
            } else {
                when(getRequiredLayout(g, Dimension.V)) {
                    Layout.DOWN, Layout.VERTICAL -> {
                        separateRectangular(va, Side.END, vb, Side.START, slackOptimisationV, Dimension.V)
                        va.mergeWithGutter(vb, slackOptimisationV)
                    }
                    Layout.UP -> {
                        separateRectangular(vb, Side.END, va, Side.START, slackOptimisationV, Dimension.V)
                        vb.mergeWithGutter(va, slackOptimisationV)
                    }
                    else -> {
                        va.mergeWithOverlap(vb, slackOptimisationV)
                    }
                }
            }
            slackOptimisationV.remove(a)
            slackOptimisationV.remove(b)
            if (vm != null) {
                slackOptimisationV.add(g, vm)
            }
            slackOptimisationV.checkConsistency()


//            if (combiningAxis(g)) {
//                // in this case, we need to store whatever is available.
//                val slackOptimisationV = c.getSlackOptimisation(Dimension.V)
//                val va = slackOptimisationV.getSlideablesFor(a)
//                val vb = slackOptimisationV.getSlideablesFor(b)
//
//                val slackOptimisationH = c.getSlackOptimisation(Dimension.H)
//                val ha = slackOptimisationH.getSlideablesFor(a)
//                val hb = slackOptimisationH.getSlideablesFor(b)
//                slackOptimisationV.add(g, va ?: vb!!)
//                slackOptimisationH.add(g, ha ?: hb!!)
//
//            }
        }

        if (horizontalAxis(g) || (g is LeafGroup)) {
            completeContainers(c, g, Dimension.H)
        }

        if (verticalAxis(g) || (g is LeafGroup)) {
            completeContainers(c, g, Dimension.V)
        }
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

    private fun combiningAxis(g: CompoundGroup) : Boolean {
        val hasChildHorizontal = horizontalAxis(g.a) || horizontalAxis(g.b)
        val hasChildVertical = verticalAxis(g.a) || verticalAxis(g.b)

        return !horizontalAxis(g) && !verticalAxis(g) && hasChildHorizontal && hasChildVertical
    }

    private fun horizontalAxis(g: Group): Boolean {
        return g.axis.isHorizontal
    }

    private fun verticalAxis(g: Group): Boolean {
        return g.axis.isVertical
    }

    private fun separateRectangular(
        a: SlideableSet<*>,
        aSide: Side,
        b: SlideableSet<*>,
        bSide: Side,
        cso: C2SlackOptimisation,
        d: Dimension
    ) {
        val aSlideables =  a.getRectangularsOnSide(aSide)
        val bSlideables = b.getRectangularsOnSide(bSide)

        val aElements = aSlideables
            .flatMap { r -> r.anchors }
            .map { a -> a.e }

        val bElements = bSlideables
            .flatMap { r -> r.anchors }
            .map { a -> a.e }

        val distance = aElements.maxOf { ae ->
            bElements.maxOf { be -> getMinimumDistanceBetween(ae, aSide, be, bSide, d, null, true) }
        }

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