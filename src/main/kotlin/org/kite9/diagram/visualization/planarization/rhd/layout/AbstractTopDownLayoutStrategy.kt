package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.common.hints.PositioningHints
import org.kite9.diagram.common.hints.PositioningHints.compareEitherXBounds
import org.kite9.diagram.common.hints.PositioningHints.compareEitherYBounds
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.position.Layout.Companion.reverse
import org.kite9.diagram.model.position.Layout.Companion.rotateAntiClockwise
import org.kite9.diagram.model.position.Layout.Companion.rotateClockwise
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

/**
 * Provides the basic code for a top-down approach to laying out groups, but doesn't specify
 * either the algorithm used to choose the approach or the ordering of the groups.
 *
 * @author robmoffat
 */
abstract class AbstractTopDownLayoutStrategy(val rh: RoutableHandler2D) : LayoutStrategy, Logable {

    @JvmField
	var log = Kite9Log(this)

    private fun chooseBestCompoundGroupPlacement(gg: CompoundGroup) {
        rh.clearTempPositions(true)
        rh.clearTempPositions(false)
        val gt = gg.axis
        val ld = gg.layout
        val canBeHoriz = gt.isHorizontal && gt.isLayoutRequired
        val canBeVert = gt.isVertical && gt.isLayoutRequired
        val horizLayoutUnknown = (ld == null || ld === Layout.HORIZONTAL) && canBeHoriz
        val vertLayoutUnknown = (ld == null || ld === Layout.VERTICAL) && canBeVert
        val canDecideLayout = horizLayoutUnknown || vertLayoutUnknown
        log.send("Group A: " + gg.a)
        log.send("Group B: " + gg.b)
        if (canDecideLayout) {
            val layoutNeeded = groupsNeedLayout(gg.a, gg.b, horizLayoutUnknown, vertLayoutUnknown, ld)
            if (layoutNeeded) {
                // this is the expensive part - layout is required. Choose one
                val hintedLayout = getHintedLayout(gg, canBeHoriz, canBeVert, ld)
                var best: PlacementApproach? = null
                best = tryPlacement(
                    gg,
                    best,
                    filterLayout(hintedLayout, horizLayoutUnknown, vertLayoutUnknown),
                    canBeHoriz,
                    canBeVert,
                    true
                )
                best = tryPlacement(
                    gg,
                    best,
                    filterLayout(reverse(hintedLayout), horizLayoutUnknown, vertLayoutUnknown),
                    canBeHoriz,
                    canBeVert,
                    false
                )
                best = tryPlacement(
                    gg,
                    best,
                    filterLayout(rotateClockwise(hintedLayout), horizLayoutUnknown, vertLayoutUnknown),
                    canBeHoriz,
                    canBeVert,
                    false
                )
                best = tryPlacement(
                    gg,
                    best,
                    filterLayout(rotateAntiClockwise(hintedLayout), horizLayoutUnknown, vertLayoutUnknown),
                    canBeHoriz,
                    canBeVert,
                    false
                )
                best?.choose()
            }
        } else {
            val pa = createPlacementApproach(gg, ld, canBeHoriz, canBeVert, true)
            pa.choose()
            log.send("Group layout = $ld")
        }
    }

    private fun filterLayout(naturalLayout: Layout?, horiz: Boolean, vert: Boolean): Layout? {
        return when (naturalLayout) {
            Layout.LEFT, Layout.RIGHT -> if (horiz) naturalLayout else null
            Layout.DOWN, Layout.UP -> if (vert) naturalLayout else null
            else -> throw LogicException("Layout should be definite for an approach: $naturalLayout")
        }
    }

    /**
     * Layout can be hinted either by the [PositioningHints] of the groups, or by the ordinal
     * order of the elements in the container.  If the layout of the container is HORIZONTAL or
     * VERTICAL, favour the ordinal, otherwise favour the [PositioningHints] where available.
     */
    private fun getHintedLayout(gg: CompoundGroup, setHoriz: Boolean, setVert: Boolean, prescribed: Layout?): Layout? {
        var bx: Int? = null
        var by: Int? = null
        var out: Layout? = null
        val a = gg.a
        val b = gg.b
        if (setVert) {
            if (prescribed === Layout.VERTICAL) {
                return getVerticalOrdinalLayout(a, b)
            } else if (prescribed == null) {
                by = compareEitherYBounds(a.hints, b.hints)
                out = getVerticalOrdinalLayout(a, b)
            }
        }
        if (setHoriz) {
            if (prescribed === Layout.HORIZONTAL) {
                return getHorizontalOrdinalLayout(a, b)
            } else if (prescribed == null) {
                bx = compareEitherXBounds(a.hints, b.hints)
                out = getHorizontalOrdinalLayout(a, b)
            }
        }
        if (bx != null) {
            if (1 == bx) {
                return Layout.LEFT
            } else if (-1 == bx) {
                return Layout.RIGHT
            }
        }
        if (by != null) {
            if (1 == by) {
                return Layout.UP
            } else if (-1 == by) {
                return Layout.DOWN
            }
        }
        return out
    }

    private fun getHorizontalOrdinalLayout(a: Group, b: Group): Layout {
        return if (a.groupOrdinal < b.groupOrdinal) Layout.RIGHT else Layout.LEFT
    }

    private fun getVerticalOrdinalLayout(a: Group, b: Group): Layout {
        return if (a.groupOrdinal < b.groupOrdinal) Layout.DOWN else Layout.UP
    }

    private fun groupsNeedLayout(
        a: Group,
        b: Group,
        horizLayoutUnknown: Boolean,
        vertLayoutUnknown: Boolean,
        l: Layout?
    ): Boolean {
        if (groupsOverlap(a, b)) {
            return true
        }
        val straightVerticals = groupsHaveStraightEdges(a, b, false)
        if ((horizLayoutUnknown || l === Layout.VERTICAL) && straightVerticals) {
            return true
        }
        val straightHorizontals = groupsHaveStraightEdges(a, b, true)
        return if ((vertLayoutUnknown || l === Layout.HORIZONTAL) && straightHorizontals) {
            true
        } else false
    }

    private fun groupsHaveStraightEdges(a: Group, b: Group, horiz: Boolean): Boolean {
        val mask = DirectedLinkManager.createMask(
            null,
            false,
            false,
            if (horiz) Direction.LEFT else Direction.UP,
            if (horiz) Direction.RIGHT else Direction.DOWN
        )
        val aHasLinks = a.linkManager.subset(mask).size > 0
        val bHasLinks = b.linkManager.subset(mask).size > 0
        return aHasLinks || bHasLinks
    }

    private fun groupsOverlap(a: Group, b: Group): Boolean {
        val ari = a.axis.getPosition(rh, true)
        val bri = b.axis.getPosition(rh, true)
        return rh.overlaps(ari, bri)
    }

    private fun tryPlacement(
        gg: CompoundGroup,
        best: PlacementApproach?,
        d: Layout?,
        setHoriz: Boolean,
        setVert: Boolean,
        natural: Boolean
    ): PlacementApproach? {
        if (d != null && (best == null || best.score > 0)) {
            val newpl = createPlacementApproach(gg, d, setHoriz, setVert, natural)
            newpl.evaluate()
            log.send(if (log.go()) null else "${gg.groupNumber} going $d  score: ${newpl.score}")
            return if (best == null) {
                newpl
            } else if (best.score <= newpl.score + TOLERANCE) {
                best
            } else if (best.score >= newpl.score + TOLERANCE) {
                newpl
            } else {
                if (best.natural) {
                    best
                } else if (newpl.natural) {
                    newpl
                } else {
                    best
                }
            }
        }
        return best
    }

    protected abstract fun createPlacementApproach(
        gg: CompoundGroup, ld: Layout?,
        setHoriz: Boolean, setVert: Boolean, natural: Boolean
    ): PlacementApproach

    private fun chooseBestPlacement(lq: LayoutQueue) {
        var g = lq.poll()
        while (g != null) {
            g.axis.getPosition(rh, false)
            log.send(if (log.go()) null else "Ordering " + g.groupNumber + " size=" + g.size + " links=" + g.linkManager.linkCount)
            val out = StringBuilder(1000)
            log.send(out.toString())
            if (g is CompoundGroup) {
                val cg = g
                chooseBestCompoundGroupPlacement(cg)
                lq.complete(cg)
                lq.offer(cg.a)
                lq.offer(cg.b)
            }
            g = lq.poll()
        }
    }

    override val prefix: String
        get() = "TDLS"
    override val isLoggingEnabled: Boolean
        get() = true

    override fun layout(mr: GroupResult, lq: LayoutQueue) {
        val g = mr.groups().iterator().next()
        lq.offer(g)
        chooseBestPlacement(lq)
    }

    companion object {
        private const val TOLERANCE = 0.0000001
    }
}