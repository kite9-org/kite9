package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.Side
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
                    separateRectangular(ha, Side.END, hb, Side.START, slackOptimisationH, Dimension.H)
                    vm = va.merge(vb, slackOptimisationV)
                    hm = ha.mergeWithAxis(hb, slackOptimisationH)
                }
                Layout.UP -> {
                    separateRectangular(hb, Side.END, ha, Side.START, slackOptimisationH, Dimension.H)
                    vm = va.merge(vb, slackOptimisationV)
                    hm = hb.mergeWithAxis(ha, slackOptimisationH)
                }
                Layout.RIGHT -> {
                    separateRectangular(va, Side.END, vb, Side.START, slackOptimisationV, Dimension.V)
                    vm = va.mergeWithAxis(vb, slackOptimisationV)
                    hm = hb.merge(ha, slackOptimisationH)
                }
                Layout.LEFT -> {
                    separateRectangular(vb, Side.END, va, Side.START, slackOptimisationV, Dimension.V)
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

    private fun separateRectangular(
        a: RectangularSlideableSet,
        aSide: Side,
        b: RectangularSlideableSet,
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
            bElements.maxOf { be -> getMinimumDistanceBetween(ae, aSide, be, bSide, d, null, false) }
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