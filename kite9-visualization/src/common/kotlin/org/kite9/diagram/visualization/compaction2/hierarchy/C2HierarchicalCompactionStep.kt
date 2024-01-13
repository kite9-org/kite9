package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.AbstractC2ContainerCompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.SlideableSet
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

            if (horizontalAxis(g)) {
                // horizontal axis
                val slackOptimisationH = c.getSlackOptimisation(Dimension.H)
                val ha = slackOptimisationH.getSlideablesFor(a)
                val hb = slackOptimisationH.getSlideablesFor(b)
                val hm = if ((ha == null) || (hb == null)) {
                    ha ?: hb
                } else {
                    val rl = getRequiredLayout(g, Dimension.H)
                    when (rl) {
                        Layout.RIGHT -> {
                            separateRectangular(ha, hb, slackOptimisationH, Dimension.H)
                            ha.mergeWithGutter(hb, slackOptimisationH)
                        }

                        Layout.LEFT -> {
                            separateRectangular(hb, ha, slackOptimisationH, Dimension.H)
                            hb.mergeWithGutter(ha, slackOptimisationH)
                        }

                        null -> {
                            hb.mergeWithOverlap(ha, slackOptimisationH)
                        }

                        else -> {
                            throw LogicException("Shouldn't get an x-axis merge of $rl for group $g")
                        }
                    }
                }

                slackOptimisationH.remove(a)
                slackOptimisationH.remove(b)

                if (hm != null) {
                    slackOptimisationH.add(g, hm)
                }
                slackOptimisationH.checkConsistency()
            }

            if (verticalAxis(g)) {
                // vertical axis
                val slackOptimisationV = c.getSlackOptimisation(Dimension.V)
                val va = slackOptimisationV.getSlideablesFor(a)
                val vb = slackOptimisationV.getSlideablesFor(b)
                val vm = if ((va == null) || (vb == null)) {
                    va ?: vb
                } else {
                    val rl = getRequiredLayout(g, Dimension.V)
                    when (rl) {
                        Layout.DOWN -> {
                            separateRectangular(va, vb, slackOptimisationV, Dimension.V)
                            va.mergeWithGutter(vb, slackOptimisationV)
                        }

                        Layout.UP -> {
                            separateRectangular(vb, va, slackOptimisationV, Dimension.V)
                            vb.mergeWithGutter(va, slackOptimisationV)
                        }

                        null -> {
                            va.mergeWithOverlap(vb, slackOptimisationV)
                        }

                        else -> {
                            throw LogicException("Shouldn't get an x-axis merge of $rl for group $g")
                        }
                    }
                }

                slackOptimisationV.remove(a)
                slackOptimisationV.remove(b)

                if (vm != null) {
                    slackOptimisationV.add(g, vm)
                }
                slackOptimisationV.checkConsistency()
            }

            if (combiningAxis(g)) {
                // in this case, we need to store whatever is available.
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

        if (horizontalAxis(g)) {
            completeContainers(c, g, Dimension.H)
        }

        if (verticalAxis(g)) {
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