package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.algorithms.ssp.NoFurtherPathException
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.anchors.AnchorType
import org.kite9.diagram.visualization.compaction2.anchors.ConnAnchor
import org.kite9.diagram.visualization.compaction2.hierarchy.AbstractC2BuilderCompactionStep
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.links.RankBasedConnectionQueue
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D
import kotlin.math.max


class C2ConnectionRouterCompactionStep(cd: CompleteDisplayer, gp: GridPositioner) :
    AbstractC2BuilderCompactionStep(cd) {

    override val prefix = "C2CR"
    override val isLoggingEnabled = true

    private fun createPoints(
        c2: C2Compaction,
        d: Connected,
        arriving: Boolean,
        drawDirection: Direction?
    ): Set<C2Point> {
        val h = c2.getSlackOptimisation(Dimension.H)
        val v = c2.getSlackOptimisation(Dimension.V)
        val hss = h.getSlideablesFor(d)!!
        val vss = v.getSlideablesFor(d)!!

        val up = if (allowed(arriving, drawDirection, Direction.UP)) {
                c2.getIntersections(vss.l)
                    ?.map { C2Point(it, vss.l, if (arriving) Direction.DOWN else Direction.UP) } ?: emptyList()
            } else {
                emptyList()
            }

        val down = if (allowed(arriving, drawDirection, Direction.DOWN)) {
            c2.getIntersections(vss.r)
                ?.map { C2Point(it, vss.r, if (arriving) Direction.UP else Direction.DOWN) } ?: emptyList()
            } else {
                emptyList()
            }

        val left = if (allowed(arriving, drawDirection, Direction.LEFT)) {
            c2.getIntersections(hss.l)
                ?.map { C2Point(it, hss.l, if (arriving) Direction.RIGHT else Direction.LEFT) } ?: emptyList()
            } else {
                emptyList()
            }

        val right = if (allowed(arriving, drawDirection, Direction.RIGHT)) {
            c2.getIntersections(hss.r)
                ?.map { C2Point(it, hss.r, if (arriving) Direction.LEFT else Direction.RIGHT) } ?: emptyList()
        } else {
            emptyList()
        }

        return (up+down+left+right).toSet()
    }

private fun allowed(arriving: Boolean, drawDirection: Direction?, d: Direction): Boolean {
    return if (drawDirection == null) {
        true
    } else {
        val dd = if (arriving) { Direction.reverse(drawDirection) } else { drawDirection }
        dd == d
    }
}

    private fun createZone(c2: C2Compaction, r: Rectangular): Zone {
        val h = c2.getSlackOptimisation(Dimension.H)
        val hs = h.getSlideablesFor(r)!!
        val v = c2.getSlackOptimisation(Dimension.V)
        val vs = v.getSlideablesFor(r)!!
        return Zone(
            hs.l, hs.r,
            vs.l, vs.r
        )
    }





    private fun insertLink(c2: C2Compaction, c: Connection): C2Route? {
        try {
            val d = if (c.getRenderingInformation().isContradicting) { null } else { c.getDrawDirection() }
            val startingPoints = createPoints(c2, c.getFrom(), false, d)
            val endingPoints = createPoints(c2, c.getTo(), true, d)
            val endZone = createZone(c2, c.getTo() as Rectangular)

            // we might be able to reduce the call frequency of this - it's expensive
            c2.getSlackOptimisation(Dimension.H).updateTDMatrix()
            c2.getSlackOptimisation(Dimension.V).updateTDMatrix()

            val doer = C2SlideableSSP(
                c, startingPoints, endingPoints, c.getFrom(), c.getTo(), endZone, d, c2,
                c2.getSlackOptimisation(Dimension.H).getTransitiveDistanceMatrix(),
                c2.getSlackOptimisation(Dimension.V).getTransitiveDistanceMatrix(),
                log
            )

            log.send("Starting Points", startingPoints)
            log.send("Ending Points", endingPoints)

            log.send("Routing: $c via", doer.allowedToLeave)
            val out = doer.createShortestPath()

            log.send("Found shortest path: $out")

            val replacements = mutableMapOf<C2Slideable, C2Slideable>()
            val out2 = simplifyShortestPath(out, c2, doer, replacements)
            val out3 = handleReplacements(out2, replacements)!!

            if (out3.prev != null) {
                ensureSlackForConnection(out3.point, out3.prev, c, true)
            }
            c2.checkConsistency()

            return out3

        } catch (e: NoFurtherPathException) {
            log.error("Couldn't route: $c")
            return null
        }
    }

    private fun handleReplacements(p: C2Point, replacements: Map<C2Slideable, C2Slideable>) : C2Point {
        return if (replacements.containsKey(p.a) || replacements.containsKey(p.b)) {
            val newA = replacements[p.a] ?: p.a
            val newB = replacements[p.b] ?: p.b
            handleReplacements(C2Point(newA, newB, p.d), replacements)
        } else {
            p
        }
    }

    private fun handleReplacements(r: C2Route?, replacements: Map<C2Slideable, C2Slideable>) : C2Route? {
        return if (r != null) {
            C2Route(handleReplacements(r.prev, replacements), handleReplacements(r.point, replacements), r.cost)
        } else {
            null
        }
    }



    /**
     * This looks for opportunities to merge slideables where there is a "dog-leg"
     * in the routing.
     */
    private fun simplifyShortestPath(
        r: C2Route,
        c2: C2Compaction,
        ssp: C2SlideableSSP,
        replacements: MutableMap<C2Slideable, C2Slideable>
    ): C2Route {
        val second = r.prev
        val third = r.prev?.prev

        if ((third != null) && (third.point.d == r.point.d)) {
            val s1 = third.point.getAlong()
            val s2 = r.point.getAlong()

            val absDist = ssp.getAbsoluteDistance(s1, s2)
            if (absDist == 0) {
                // ok, these slideables can be merged then we can simplify the route
                val so = third.point.getAlong().so as C2SlackOptimisation
                val ns = so.mergeSlideables(s1, s2)!!
                updateReplacements(s1, s2, ns, replacements)

                // remove the second, rewrite the first
                val nsp = handleReplacements(third, replacements)!!
                val newFirst = handleReplacements(r.point, replacements)
                return C2Route(
                    simplifyShortestPath(nsp, c2, ssp, replacements),
                    C2Point(ns, newFirst.getPerp(), newFirst.d),
                    r.cost
                )
            }
        }

        return if (second != null) {
            C2Route(simplifyShortestPath(second, c2, ssp, replacements), r.point, r.cost)
        } else {
            r
        }
    }

    private fun updateReplacements(s1: C2Slideable, s2: C2Slideable, ns: C2Slideable, replacements: MutableMap<C2Slideable, C2Slideable>) {
        if (s1 != ns) {
            replacements[s1] = ns
            val keysToUpdate = replacements.filter { (_,v) -> v == s1 }.keys
            keysToUpdate.forEach { replacements[it] = ns }
        }

        if (s2 != ns) {
            replacements[s2] = ns
            val keysToUpdate = replacements.filter { (_,v) -> v == s2 }.keys
            keysToUpdate.forEach { replacements[it] = ns }
        }

    }

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

        queue.forEach {
            if (it is Connection) {
                val route = insertLink(c, it)
                if (route != null) {
                    writeRoute(it, route, 0)
                }
            }
        }

        println("Done")
    }

    private fun writeRoute(c: Connection, r: C2Route?, i: Int) {
        if (r != null) {
            val p = r.point
            val terminal = (i == 0) || (r.prev == null)
            val anchorType =  if (terminal) { AnchorType.TERMINAL} else { AnchorType.REGULAR}
            p.getAlong().addAnchor(ConnAnchor(c, i.toFloat(),anchorType))
            p.getPerp().addAnchor(ConnAnchor(c, i.toFloat(), anchorType))
            writeRoute(c, r.prev, i + 1)
        }
    }

}