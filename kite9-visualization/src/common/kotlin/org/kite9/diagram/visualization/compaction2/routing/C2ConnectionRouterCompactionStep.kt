package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.algorithms.ssp.NoFurtherPathException
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.builders.AbstractC2BuilderCompactionStep
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.links.RankBasedConnectionQueue
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class C2ConnectionRouterCompactionStep(cd: CompleteDisplayer, gp: GridPositioner) :
    AbstractC2BuilderCompactionStep(cd, gp) {

    override val prefix = "C2CR"
    override val isLoggingEnabled = true

    private val usedStartEndPoints = mutableSetOf<C2Point>()

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

        val hInter = h.getAllSlideables()
            .filterIsInstance<C2IntersectionSlideable>()
            .filter { it.intersects.contains(d) }

        val vInter = v.getAllSlideables()
            .filterIsInstance<C2IntersectionSlideable>()
            .filter { it.intersects.contains(d) }

        val vertical = hInter.flatMap {
            listOf(
                C2Point(it, vss.l, if (arriving) Direction.DOWN else Direction.UP),  // top
                C2Point(it, vss.r, if (arriving) Direction.UP else Direction.DOWN),  // bottom
            )
        }.toSet()

        val horizontal = vInter.flatMap {
            listOf(
                C2Point(it, hss.l, if (arriving) Direction.RIGHT else Direction.LEFT),  // left
                C2Point(it, hss.r, if (arriving) Direction.LEFT else Direction.RIGHT),   // right
            )
        }.toSet()

        return (vertical + horizontal - usedStartEndPoints)
            .filter { (it.d == drawDirection) || (drawDirection == null) }
            .toSet()
    }

    private fun createZone(c2: C2Compaction, r: Rectangular): Zone {
        val h = c2.getSlackOptimisation(Dimension.H)
        val hs = h.getSlideablesFor(r)!!
        val v = c2.getSlackOptimisation(Dimension.V)
        val vs = v.getSlideablesFor(r)!!
        return Zone(
            hs.l.minimumPosition, hs.r.maximumPosition!!,
            vs.l.minimumPosition, vs.r.maximumPosition!!
        )
    }

    /**
     * Implementation of Floyd-Warshall algorithm for transitive distances.  Runs in o(n^3)
     */
    private fun createTDMatrix(so: C2SlackOptimisation): Map<C2Slideable, Map<C2Slideable, Int>> {
        val out = mutableMapOf<C2Slideable, MutableMap<C2Slideable, Int>>()

        // set initial constraints

        so.getAllSlideables().forEach { f ->
            so.getAllSlideables().forEach { t ->
                if (f != t) {
                    val d1 = f.getMinimumForwardConstraintTo(t)
                    val d2 = t.getMinimumForwardConstraintTo(f)

                    if ((d1 != null) || (d2 != null)) {

                        val mapFrom = out.getOrPut(f) { mutableMapOf<C2Slideable, Int>() }
                        val mapTo = out.getOrPut(t) { mutableMapOf<C2Slideable, Int>() }

                        if (d1 != null) {
                            mapFrom.put(t, d1)
                            mapTo.put(f, -d1)
                        } else if (d2 != null) {
                            mapFrom.put(t, -d2)
                            mapTo.put(f, d2)
                        }
                    }
                }
            }
        }

        so.getAllSlideables().forEach { k ->
            val kMap = out.getOrPut(k) { mutableMapOf<C2Slideable, Int>() }
            so.getAllSlideables().forEach { f ->
                if (f != k) {
                    val fromMap = out.getOrPut(f) { mutableMapOf<C2Slideable, Int>() }
                    so.getAllSlideables().forEach { t ->
                        if ((t != f) && (t != k)) {
                            val ft = fromMap[t] ?: 0
                            val kt = kMap[t]
                            val fk = fromMap[k]
                            val vk = if ((fk != null) && (kt != null)) fk + kt else null
                            if (vk != null) {
                                if ((kt!! > 0) && (fk!! > 0)) {
                                    fromMap[t] = max(vk, ft)
                                } else if ((kt!! < 0) && (fk!! < 0)) {
                                    fromMap[t] = min(vk, ft)
                                }
                            }
                        }
                    }
                }
            }
        }



        return out
    }

    private fun insertLink(c2: C2Compaction, c: Connection): C2Route? {
        try {
            val startingPoints = createPoints(c2, c.getFrom(), false, c.getDrawDirection())
            val endingPoints = createPoints(c2, c.getTo(), true, c.getDrawDirection())
            val endZone = createZone(c2, c.getTo() as Rectangular)
            val doer = C2SlideableSSP(
                startingPoints, endingPoints, c.getFrom(), c.getTo(), endZone, c.getDrawDirection(), c2.junctions,
                createTDMatrix(c2.getSlackOptimisation(Dimension.H)),
                createTDMatrix(c2.getSlackOptimisation(Dimension.V)),
                log
            )
            log.send("Routing: $c via", doer.allowedTraversal)
            val out = doer.createShortestPath()

            log.send("Found shortest path: $out")

            val out2 = simplifyShortestPath(out, c2)

            updateUsedStartEndPoints(out2.point, c.getTo())
            updateUsedStartEndPoints(getStart(out2), c.getFrom())

            if (out2.prev != null) {
                ensureSlackForConnection(out2.point, out2.prev, c, true)
            }

            return out2

        } catch (e: NoFurtherPathException) {
            log.error("Couldn't route: $c")
            return null
        }
    }

    /**
     * This looks for opportunities to merge slideables where there is a "dog-leg"
     * in the routing.
     */
    private fun simplifyShortestPath(r: C2Route, c2: C2Compaction): C2Route {
        var first = r
        var second = r?.prev ?: null
        val third = r?.prev?.prev ?: null

        if (third != null) {
            if (third.point.d == first.point.d) {
                val s1 = third.point.getAlong()
                val s2 = first.point.getAlong()
                if (C2SlideableSSP.intersects(s1, s2)) {
                    // ok, these slideables can be merged and we can simplify the route
                    val ssp = third.point.getAlong().so as C2SlackOptimisation
                    val ns = ssp.mergeSlideables(s1 as C2BufferSlideable, s2 as C2BufferSlideable)

                    first = third
                    second = third.prev
                }
            }
        }

        if (second != null) {
            return C2Route(simplifyShortestPath(second, c2), first.point, first.cost)
        } else {
            return first
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

        c.consistentJunctions()
        c.resortJunctions()

        val vso = c.getSlackOptimisation(Dimension.V)
        val hso = c.getSlackOptimisation(Dimension.H)

//        visitRectangulars(c.getDiagram()) {
//            ensureCentreSlideablePosition(hso, it)
//            ensureCentreSlideablePosition(vso, it)
//        }

        queue.forEach {
            if (it is Connection) {
                val route = insertLink(c, it)
                if (route != null) {
                    val startPoint = getStart(route)
                    val endPoint = route.point
                    writeRoute(it, route, 0)
                    hso.checkConsistency()
                    handleLabel(it.getFromLabel(), startPoint, c, startPoint.d, it.getFrom())
                    handleLabel(
                        it.getToLabel(),
                        getUpdatedPoint(endPoint, c, it),
                        c,
                        Direction.reverse(endPoint.d)!!,
                        it.getTo()
                    )
                    hso.checkConsistency()
                }
            }
        }
    }

    private fun getUpdatedSlideable(
        old: C2RectangularSlideable,
        so: C2SlackOptimisation,
        conn: Connection
    ): C2Slideable? {
        val connAnchor = old.anchors.first { it.e == conn }
        return so.getAllSlideables()
            .filterIsInstance<C2RectangularSlideable>()
            .firstOrNull { it.anchors.contains(connAnchor) }
    }

    private fun getUpdatedPoint(p: C2Point, c: C2Compaction, conn: Connection): C2Point {
        val oldAlong = p.getAlong() as C2RectangularSlideable
        val oldPerp = p.getPerp() as C2RectangularSlideable
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

            val rssh = checkCreate(l, Dimension.H, csoh, null, null)!!
            val rssv = checkCreate(l, Dimension.V, csov, null, null)!!

            val horiz = Direction.isHorizontal(d)
            val hc = (if (!horiz) start.getAlong() else start.getPerp()) as C2RectangularSlideable
            val vc = (if (!horiz) start.getPerp() else start.getAlong()) as C2RectangularSlideable

            // really simple merge for now
            when (d) {
                Direction.UP -> {
                    ensureDistance(l, dest, Dimension.H, csov)
                    ensureDistanceFromBuffer(l, getOrbitSlideable(dest, Side.END, csov)!!, Dimension.H, csov)
                    csoh.mergeSlideables(hc, rssh.l)
                }

                Direction.LEFT -> {
                    ensureDistance(l, dest, Dimension.V, csoh)
                    ensureDistanceFromBuffer(l, getOrbitSlideable(dest, Side.END, csoh)!!, Dimension.V, csoh)
                    csov.mergeSlideables(vc, rssv.l)
                }

                Direction.DOWN -> {
                    ensureDistance(dest, l, Dimension.H, csov)
                    ensureDistanceFromBuffer(l, getOrbitSlideable(dest, Side.END, csov)!!, Dimension.H, csov)
                    csoh.mergeSlideables(hc, rssh.l)
                }

                Direction.RIGHT -> {
                    ensureDistance(dest, l, Dimension.V, csoh)
                    ensureDistanceFromBuffer(l, getOrbitSlideable(dest, Side.END, csoh)!!, Dimension.V, csoh)
                    csov.mergeSlideables(vc, rssv.l)
                }
            }

            inside(l, dest, Dimension.H, csoh)
            inside(l, dest, Dimension.V, csov)
        }
    }

    private fun ensureDistanceFromBuffer(bs: C2BufferSlideable, to: Positioned, d: Dimension, so: C2SlackOptimisation) {
        val rs = getRectangularSlideable(to, Side.START, so)!!
        so.ensureMinimumDistance(bs, rs, 0)
        bs.getForwardSlideables(false)
            .filterIsInstance<C2RectangularSlideable>()
            .forEach { ls ->
                ensureMinimumDistanceBetweenRectangularSlideables(ls, rs, d, so)
            }
    }

    private fun ensureDistanceFromBuffer(
        from: Positioned,
        bs: C2BufferSlideable,
        d: Dimension,
        so: C2SlackOptimisation
    ) {
        val ls = getRectangularSlideable(from, Side.END, so)!!
        so.ensureMinimumDistance(ls, bs, 0)
        bs.getForwardSlideables(true)
            .filterIsInstance<C2RectangularSlideable>()
            .forEach { rs ->
                ensureMinimumDistanceBetweenRectangularSlideables(ls, rs, d, so)
            }
    }

    private fun ensureMinimumDistanceBetweenRectangularSlideables(
        from: C2RectangularSlideable,
        to: C2RectangularSlideable,
        d: Dimension,
        so: C2SlackOptimisation
    ) {
        val dist = from.anchors.filterIsInstance<RectAnchor>()
            .maxOf { fa ->
                to.anchors
                    .filterIsInstance<RectAnchor>()
                    .maxOf { ta -> getMinimumDistanceBetween(fa.e, fa.s, ta.e, ta.s, d, null, true).toInt() }
            }


        so.ensureMinimumDistance(from, to, dist)
    }


    private fun ensureDistance(from: Positioned, to: Positioned, d: Dimension, so: C2SlackOptimisation) {
        val ld = getMinimumDistanceBetween(from, Side.END, to, Side.START, d, null, true).toInt()
        val fromSS = so.getSlideablesFor(from)!!
        val toSS = so.getSlideablesFor(to)!!
        so.ensureMinimumDistance(fromSS.r, toSS.l, ld)
    }

    private fun getOrbitSlideable(de: Positioned, side: Side, so: C2SlackOptimisation): C2OrbitSlideable? {
        return so.getAllSlideables()
            .filterIsInstance<C2OrbitSlideable>()
            .firstOrNull { os -> os.orbits.any { it.e == de && it.s == side } }
    }

    private fun getRectangularSlideable(de: Positioned, side: Side, so: C2SlackOptimisation): C2RectangularSlideable? {
        return so.getAllSlideables()
            .filterIsInstance<C2RectangularSlideable>()
            .firstOrNull { os -> os.anchors.any { it.e == de && it.s == side } }
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

    private fun writeRoute(c: Connection, r: C2Route?, i: Int) {
        if (r != null) {
            val p = r.point
            (p.getAlong() as C2RectangularSlideable).addAnchor(ConnAnchor(c, i))
            (p.getPerp() as C2RectangularSlideable).addAnchor(ConnAnchor(c, i))
            writeRoute(c, r.prev, i + 1)
        }
    }

    companion object {
        fun getStart(out: C2Route): C2Point {
            return if (out.prev != null) {
                getStart(out.prev)
            } else {
                out.point
            }
        }
    }
}