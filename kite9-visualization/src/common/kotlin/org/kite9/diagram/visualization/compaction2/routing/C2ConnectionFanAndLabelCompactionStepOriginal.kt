package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.hierarchy.AbstractC2BuilderCompactionStep
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.links.RankBasedConnectionQueue
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D
import kotlin.math.max

class C2ConnectionFanAndLabelCompactionStepOriginal(cd: CompleteDisplayer, gp: GridPositioner) :
    AbstractC2BuilderCompactionStep(cd) {

    override val prefix = "C2CR"
    override val isLoggingEnabled = true

    private val usedStartEndPoints = mutableSetOf<C2Point>()


    private fun ensureSlackForConnection(start: C2Point, rest: C2Route, c: Connection, connectionStart: Boolean) {
        val end = rest.point
        val d = start.d
        val from = start.getPerp()
        val to = end.get(from.dimension)


        // handle connection minimum length
        val connectionEnd = rest.prev == null
        val length = when {
            connectionStart && connectionEnd -> c.getFromDecoration().getReservedLength() + c.getToDecoration()
                .getReservedLength()

            connectionStart -> c.getFromDecoration().getReservedLength()
            connectionEnd -> c.getToDecoration().getReservedLength()
            else -> c.getMinimumLength()
        } + c.getMinimumLength()

        if (C2SlideableSSP.isIncreasing(start.d)) {
            from.so.ensureMinimumDistance(to, from, length.toInt())
        } else {
            from.so.ensureMinimumDistance(from, to, length.toInt())
        }

        // handle connection margins
        val margin = max(
            c.getMargin(Direction.rotateAntiClockwise(d)),
            c.getMargin(Direction.rotateClockwise(d))
        )

        val along = start.getAlong()
        along.getForwardSlideables(true).forEach {
            along.so.ensureMinimumDistance(along, it, margin.toInt())
        }

        along.getForwardSlideables(false).forEach {
            along.so.ensureMinimumDistance(it, along, margin.toInt())
        }

        // continue
        if (rest.prev != null) {
            ensureSlackForConnection(end, rest.prev, c, false)
        }
    }

    private fun updateUsedStartEndPoints(out: C2Point, from: Connected) {
        if (from is Port) {
            // ports are allowed to have multiple connections
        } else {
            usedStartEndPoints.add(out)
        }
    }

    private fun buildQueue(g: Group, queue: RankBasedConnectionQueue) {
        if (g is CompoundGroup) {
            buildQueue(g.a, queue)
            buildQueue(g.b, queue)
        }

        queue.handleLinks(g)
    }

    override fun usingGroups(contents: List<ConnectedRectangular>, topGroup: Group?) = false

    override fun compact(c: C2Compaction, g: Group) {
        val queue = RankBasedConnectionQueue(PositionRoutableHandler2D())
        buildQueue(g, queue)

        // TODO: we're going to need a connection/side based approach to this
        // where we deal with everything arriving on a given side.
        queue.forEach {
//            if (it is Connection) {
//                if (it.getFromLabel() != null) {
//                    val start = getPointAtEnd(c, it.getFrom())
//                    handleLabel(it.getFromLabel(), )
//                }
//
//                if (it.getToLabel() != null) {
//                    handleLabel(it.getToLabel())
//                }
//            }
        }

        println("Labelled")
    }

    private fun getUpdatedSlideable(
        old: C2Slideable,
        so: C2SlackOptimisation,
        conn: Connection
    ): C2Slideable? {
        val connAnchor = old.getConnAnchors().first { it.e == conn }
        return so.getAllSlideables()
            .firstOrNull { it.getConnAnchors().contains(connAnchor) }
    }

    private fun getUpdatedPoint(p: C2Point, c: C2Compaction, conn: Connection): C2Point {
        val oldAlong = p.getAlong()
        val oldPerp = p.getPerp()
        return C2Point(
            if (oldAlong.done) getUpdatedSlideable(
                oldAlong,
                c.getSlackOptimisation(oldAlong.dimension),
                conn
            )!! else oldAlong,
            if (oldPerp.done) getUpdatedSlideable(
                oldPerp,
                c.getSlackOptimisation(oldPerp.dimension),
                conn
            )!! else oldPerp,
            p.d
        )
    }

    private fun handleLabel(l: Label?, start: C2Point, c2: C2Compaction, d: Direction, dest: Connected) {
        if (l != null) {
            val csoh = c2.getSlackOptimisation(Dimension.H)
            val csov = c2.getSlackOptimisation(Dimension.V)

            val rssh = checkCreateElement(l, Dimension.H, csoh, null, null)!!
            val rssv = checkCreateElement(l, Dimension.V, csov, null, null)!!

            val horiz = Direction.isHorizontal(d)
            val hc = (if (!horiz) start.getAlong() else start.getPerp())
            val vc = (if (!horiz) start.getPerp() else start.getAlong())

            // really simple merge for now
            val leftBuffer = getOrbitSlideable(dest, Side.START, csoh)!!
            val rightBuffer = getOrbitSlideable(dest, Side.END, csoh)!!
            val topBuffer = getOrbitSlideable(dest, Side.START, csov)!!
            val bottomBuffer = getOrbitSlideable(dest, Side.END, csov)!!

            when (d) {
                Direction.UP -> {
                    ensureDistance(l, dest, Dimension.H, csov)
                    ensureDistanceFromBuffer(topBuffer, l, Dimension.H, csov)
                    csoh.mergeSlideables(hc, rssh.l)
                }

                Direction.LEFT -> {
                    ensureDistance(l, dest, Dimension.V, csoh)
                    ensureDistanceFromBuffer(leftBuffer, l, Dimension.V, csoh)
                    csov.mergeSlideables(vc, rssv.l)
                }

                Direction.DOWN -> {
                    ensureDistance(dest, l, Dimension.H, csov)
                    ensureDistanceFromBuffer(l, bottomBuffer, Dimension.H, csov)
                    csoh.mergeSlideables(hc, rssh.l)
                }

                Direction.RIGHT -> {
                    ensureDistance(dest, l, Dimension.V, csoh)
                    ensureDistanceFromBuffer(l, rightBuffer, Dimension.V, csoh)
                    csov.mergeSlideables(vc, rssv.l)
                }
            }

            inside(l, dest, Dimension.H, csoh)
            inside(l, dest, Dimension.V, csov)
        }
    }

    private fun ensureDistanceFromBuffer(bs: C2Slideable, to: Positioned, d: Dimension, so: C2SlackOptimisation) {
        val rs = getRectangularSlideable(to, Side.START, so)!!
        so.ensureMinimumDistance(bs, rs, 0)
        bs.getForwardSlideables(false)
            .forEach { ls ->
                ensureMinimumDistanceBetweenRectangularSlideables(ls, rs, d, so)
            }
    }

    private fun ensureDistanceFromBuffer(
        from: Positioned,
        bs: C2Slideable,
        d: Dimension,
        so: C2SlackOptimisation
    ) {
        val ls = getRectangularSlideable(from, Side.END, so)!!
        so.ensureMinimumDistance(ls, bs, 0)
        bs.getForwardSlideables(true)
            .forEach { rs ->
                ensureMinimumDistanceBetweenRectangularSlideables(ls, rs, d, so)
            }
    }

    private fun ensureMinimumDistanceBetweenRectangularSlideables(
        from: C2Slideable,
        to: C2Slideable,
        d: Dimension,
        so: C2SlackOptimisation
    ) {
        val dist = from.getRectangulars()
            .maxOfOrNull { fa ->
                to.getRectangulars()
                    .maxOfOrNull { ta -> getMinimumDistanceBetween(fa.e, fa.s, ta.e, ta.s, d, null, true).toInt() } ?: 0
            } ?: 0


        so.ensureMinimumDistance(from, to, dist)
    }


    private fun ensureDistance(from: Positioned, to: Positioned, d: Dimension, so: C2SlackOptimisation) {
        val ld = getMinimumDistanceBetween(from, Side.END, to, Side.START, d, null, true).toInt()
        val fromSS = so.getSlideablesFor(from)!!
        val toSS = so.getSlideablesFor(to)!!
        so.ensureMinimumDistance(fromSS.r, toSS.l, ld)
    }

    private fun getOrbitSlideable(de: Positioned, side: Side, so: C2SlackOptimisation): C2Slideable? {
        return so.getAllSlideables()
            .firstOrNull { os -> os.getOrbits().any { it.e == de && it.s == side } }
    }

    private fun getRectangularSlideable(de: Positioned, side: Side, so: C2SlackOptimisation): C2Slideable? {
        return so.getAllSlideables()
            .firstOrNull { os -> os.getRectangulars().any { it.e == de && it.s == side } }
    }

    private fun inside(innerDe: Label, outerDe: Connected, d: Dimension, so: C2SlackOptimisation) {

        val bl = getOrbitSlideable(outerDe, Side.START, so)!!
        val br = getOrbitSlideable(outerDe, Side.END, so)!!

        val inner = so.getSlideablesFor(innerDe)!!

        if (inner.l != bl) {
            ensureDistanceFromBuffer(bl, innerDe, d, so)
        }

        if (inner.r != br) {
            ensureDistanceFromBuffer(innerDe, br, d, so)
        }

    }

}