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

            val slackOptimisationH = c.getSlackOptimisation(Dimension.H)
            val slackOptimisationV = c.getSlackOptimisation(Dimension.V)

            val ha = slackOptimisationH.getSlideablesFor(a)!!
            val va = slackOptimisationV.getSlideablesFor(a)!!
            val hb = slackOptimisationH.getSlideablesFor(b)!!
            val vb = slackOptimisationV.getSlideablesFor(b)!!

            var vm : RoutableSlideableSet? = null
            var hm : RoutableSlideableSet? = null

            when(layout) {
                Layout.RIGHT -> {
                    separateRectangular(ha, Side.END, hb, Side.START, slackOptimisationH, Dimension.H)
                    vm = va.mergeWithOverlap(vb, slackOptimisationV)
                    hm = ha.mergeWithGutter(hb, slackOptimisationH)
                }
                Layout.LEFT -> {
                    separateRectangular(hb, Side.END, ha, Side.START, slackOptimisationH, Dimension.H)
                    vm = va.mergeWithOverlap(vb, slackOptimisationV)
                    hm = hb.mergeWithGutter(ha, slackOptimisationH)
                }
                Layout.DOWN -> {
                    separateRectangular(va, Side.END, vb, Side.START, slackOptimisationV, Dimension.V)
                    vm = va.mergeWithGutter(vb, slackOptimisationV)
                    hm = hb.mergeWithOverlap(ha, slackOptimisationH)
                }
                Layout.UP -> {
                    separateRectangular(vb, Side.END, va, Side.START, slackOptimisationV, Dimension.V)
                    vm = vb.mergeWithGutter(va, slackOptimisationV)
                    hm = hb.mergeWithOverlap(ha, slackOptimisationH)
                }
                else -> {
                    vm = va.mergeWithOverlap(vb, slackOptimisationV)
                    hm = hb.mergeWithOverlap(ha, slackOptimisationH)
                }
            }

            slackOptimisationH.add(g, hm)
            slackOptimisationV.add(g, vm)

            slackOptimisationH.checkConsistency()
            slackOptimisationV.checkConsistency()
        }

        super.compact(c, g)
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