package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

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
                var hm : RoutableSlideableSet? = null


                when(layout) {
                    Layout.RIGHT -> {
                        separateRectangular(ha, Side.END, hb, Side.START, slackOptimisationH, Dimension.H)
                        hm = ha.mergeWithGutter(hb, slackOptimisationH)
                    }
                    Layout.LEFT -> {
                        separateRectangular(hb, Side.END, ha, Side.START, slackOptimisationH, Dimension.H)
                        hm = hb.mergeWithGutter(ha, slackOptimisationH)
                    }
                    Layout.DOWN -> {
                        hm = hb.mergeWithOverlap(ha, slackOptimisationH)
                    }
                    Layout.UP -> {
                        hm = hb.mergeWithOverlap(ha, slackOptimisationH)
                    }
                    else -> {
                        hm = hb.mergeWithOverlap(ha, slackOptimisationH)
                    }
                }

                slackOptimisationH.add(g, hm!!)
                slackOptimisationH.checkConsistency()
                completeContainers(c, g, Dimension.H)
            }

            if (verticalAxis(g)) {
                val slackOptimisationV = c.getSlackOptimisation(Dimension.V)
                val va = slackOptimisationV.getSlideablesFor(a)!!
                val vb = slackOptimisationV.getSlideablesFor(b)!!
                var vm : RoutableSlideableSet? = null

                when(layout) {
                    Layout.RIGHT -> {
                        vm = va.mergeWithOverlap(vb, slackOptimisationV)
                    }
                    Layout.LEFT -> {
                        vm = va.mergeWithOverlap(vb, slackOptimisationV)
                    }
                    Layout.DOWN -> {
                        separateRectangular(va, Side.END, vb, Side.START, slackOptimisationV, Dimension.V)
                        vm = va.mergeWithGutter(vb, slackOptimisationV)
                    }
                    Layout.UP -> {
                        separateRectangular(vb, Side.END, va, Side.START, slackOptimisationV, Dimension.V)
                        vm = vb.mergeWithGutter(va, slackOptimisationV)
                    }
                    else -> {
                        vm = va.mergeWithOverlap(vb, slackOptimisationV)
                    }
                }

                slackOptimisationV.add(g, vm!!)
                slackOptimisationV.checkConsistency()
                completeContainers(c, g, Dimension.V)
            }
        }
    }

    private fun horizontalAxis(g: CompoundGroup): Boolean {
        return g.axis.isHorizontal
    }

    private fun verticalAxis(g: CompoundGroup): Boolean {
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