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
            val layout = g.layout


            if (horizontalAxis(g)) {
                val slackOptimisationH = c.getSlackOptimisation(Dimension.H)
                val ha = slackOptimisationH.getSlideablesFor(a)!!
                val hb = slackOptimisationH.getSlideablesFor(b)!!
                val hm = when(layout) {
                    Layout.RIGHT -> {
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

                slackOptimisationH.remove(a)
                slackOptimisationH.remove(b)
                slackOptimisationH.add(g, hm)
                slackOptimisationH.checkConsistency()
            }

            if (verticalAxis(g)) {
                val slackOptimisationV = c.getSlackOptimisation(Dimension.V)
                val va = slackOptimisationV.getSlideablesFor(a)!!
                val vb = slackOptimisationV.getSlideablesFor(b)!!
                val vm = when(layout) {
                    Layout.DOWN -> {
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
                slackOptimisationV.remove(a)
                slackOptimisationV.remove(b)
                slackOptimisationV.add(g, vm)
                slackOptimisationV.checkConsistency()
            }

            if (combiningAxis(g)) {
                // in this case, we need to
                val slackOptimisationV = c.getSlackOptimisation(Dimension.V)
                val va = slackOptimisationV.getSlideablesFor(a)
                val vb = slackOptimisationV.getSlideablesFor(b)

                val slackOptimisationH = c.getSlackOptimisation(Dimension.H)
                val ha = slackOptimisationH.getSlideablesFor(a)
                val hb = slackOptimisationH.getSlideablesFor(b)
                slackOptimisationV.add(g, va ?: vb!!)
                slackOptimisationH.add(g, ha ?: hb!!)

            }
        }

        if (horizontalAxis(g) || (g is LeafGroup)) {
            completeContainers(c, g, Dimension.H)
        }

        if (verticalAxis(g) || (g is LeafGroup)) {
            completeContainers(c, g, Dimension.V)
        }
    }

    private fun combiningAxis(g: CompoundGroup) : Boolean {
        val hasChildHorizontal = horizontalAxis(g.a) || horizontalAxis(g.b)
        val hasChildVertical = verticalAxis(g.a) || verticalAxis(g.b)

        return !horizontalAxis(g) && !verticalAxis(g) && hasChildHorizontal && hasChildVertical
    }

    private fun horizontalAxis(g: Group): Boolean {
        return g.axis.isHorizontal && g.axis.isLayoutRequired
    }

    private fun verticalAxis(g: Group): Boolean {
        return g.axis.isVertical && g.axis.isLayoutRequired
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