package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.algorithms.ssp.AbstractSSP
import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.compaction2.Constraint
import org.kite9.diagram.visualization.compaction2.anchors.RectAnchor

class C2SlideableSSP(
    val e: Connection,
    val start: Set<C2Point>,
    val end: Set<C2Point>,
    private val startElem: DiagramElement,
    private val endElem: DiagramElement,
    private val endZone: Zone,
    private val direction: Direction?,
    private val c2: C2Compaction,
    private val hMatrix: Map<C2Slideable, Map<C2Slideable, Constraint>>,
    private val vMatrix: Map<C2Slideable, Map<C2Slideable, Constraint>>,
    val log: Kite9Log
) : AbstractSSP<C2Route>() {

    override fun pathComplete(r: C2Route): Boolean {
        return end.contains(r.point) && if (direction != null) {
            r.point.d == direction
        } else {
            true
        }
    }

    val allowedToLeave : Map<DiagramElement, Int> = run {
        var se = startElem
        var ee = endElem
        val out = mutableSetOf<DiagramElement>()
        while (se != ee) {
            out.add(se)
            out.add(ee)

            if (se.getDepth() > ee.getDepth()) {
                se = se.getParent()!!
            } else if (ee.getDepth() > se.getDepth()) {
                ee = ee.getParent()!!
            } else {
                se = se.getParent()!!
                ee = ee.getParent()!!
            }
        }

        out.add(ee)

        out.map { it to it.getDepth() }.toMap()
    }

    override fun generateSuccessivePaths(r: C2Route, s: State<C2Route>) {
        val along = r.point.getAlong()
        val perp = r.point.getPerp()
        val d = r.point.d
        log.send("Extending: $r")

        // straight line advancement
        advance(d, perp, r, along, s, r.cost.addStep())

        if (perp.getRectAnchors().isEmpty()) {
            val dc = Direction.rotateClockwise(d)
            val nextScoreDc = r.cost.addTurn(CostFreeTurn.CLOCKWISE)
            advance(dc, along, r, perp, s, nextScoreDc)


            val dac = Direction.rotateAntiClockwise(d)
            val nextScoreDac = r.cost.addTurn(CostFreeTurn.ANTICLOCKWISE)
            advance(dac, along, r, perp, s, nextScoreDac)
        }
    }

    private fun advance(
        d: Direction,
        perp: C2Slideable,
        r: C2Route,
        along: C2Slideable,
        s: State<C2Route>,
        c: C2Costing,
        skipInitialCheck: Boolean = false
    ) {
        val cost2 = if (skipInitialCheck) c else canAdvancePast(perp, along, r, d, c)

        if (cost2 !== null) {
            val leavers = getForwardSlideables(along, perp, d)
            leavers.forEach { k ->
                if (r.coords.isInBounds(k.key, d)) {
                    val p = C2Point(along, k.key, d)
                    val stride = r.coords.distanceTo(p)
                    val possibleRemainingDistance = getMinimumRemainingDistance(k.key)
                    val expensive = expensiveDirection(p)
                    val newCost = cost2.addDistance(stride, possibleRemainingDistance, expensive)
                    val r3 = C2Route(r, p, newCost)
                    if (s.add(r3)) {
                        log.send("Added (${k.value}): $r3")
                    }
                }
            }
        }
    }


    private fun getAbsoluteDistance(from: C2Slideable, to: C2Slideable): Int {
        val mat = when (from.dimension) {
            Dimension.H -> hMatrix
            Dimension.V -> vMatrix
        }

        return mat[from]!![to]?.dist ?: 0
    }

    private fun getMinimumRemainingDistance(from: C2Slideable): Int {
        return end.map { it.get(from.dimension) }
            .map { getAbsoluteDistance(it, from) }
            .minOfOrNull { it } ?: 0
    }

    private fun getCorrectDistanceMatrix(from: C2Slideable, stops: Collection<C2Slideable>) : Map<C2Slideable, Constraint?> {
        val mat = when (from.dimension) {
            Dimension.H -> hMatrix
            Dimension.V -> vMatrix
        }

        val distances = mat.get(from)!!
        val known = distances.filter { stops.contains(it.key) }
        val unknown = stops.filter { !distances.containsKey(it) }.associateWith { null }
        return (known + unknown).minus(from)
    }

    private fun collectInDirection(from: C2Slideable, forward: Boolean, intersections: Set<C2Slideable>, d: Direction, along: C2Slideable) : Map<C2Slideable, Constraint?> {
        val stopsDistances = getCorrectDistanceMatrix(from, intersections)

        // remove all the ones in the wrong direction
        val stopDistRightDirection = stopsDistances.filter { it.value != null && it.value!!.forward == forward }
        val unboundedStops = stopsDistances.filter { it.value == null }

//        val potentialBlockers = getBlockingIntersections(stopDistRightDirection.keys, d, along)
//            .map { it to stopDistRightDirection[it] }
//            .filter { (_, t) -> t != null }
//            .associate { (f, t) -> f to t!! }

        val closestBlocker = stopDistRightDirection.minByOrNull { it.value!!.dist }

        if (closestBlocker != null) {
            return unboundedStops.plus(closestBlocker.toPair())
        }

        return unboundedStops
    }

    private fun getForwardSlideables(along: C2Slideable, startingAt: C2Slideable, going: Direction) : Map<C2Slideable, Constraint?> {
        val forward = when(going) {
            Direction.DOWN, Direction.RIGHT -> true
            Direction.LEFT, Direction.UP -> false
        }
        val out = collectInDirection(startingAt, forward, c2.getIntersections(along) ?: emptySet(), going, along)
        return out
    }

    private fun canAdvancePast(perp: C2Slideable, along: C2Slideable, routeIn: C2Route, d: Direction, c: C2Costing): C2Costing? {
        // this is approximate - might need improvement later
        val blocking = perp.isBlocker(d, along)

        if (blocking) {
            return null // can't go this way
        }

        val rectangulars = perp.getRectAnchors()
        val intersections = along.getIntersectingElements()
        val orbits = along.getOrbitingElements()

        if (rectangulars.isEmpty() || (intersections.isEmpty() && orbits.isEmpty())) {
            // short-cut the effort
            return c
        }

        val crossingRectangulars = rectangulars.filter {
            containsTheIntersection(intersections, it) || containsTheOrbit(orbits, it)
        }

        if (crossingRectangulars.isEmpty()) {
            return c
        } else if (crossingRectangulars.size == 1) {
            val entering = crossingRectangulars.first().s.isEntering(d)
            val oldDepth = routeIn.cost.containerDepth
            return c.addCrossing(true, if (entering) oldDepth+1 else oldDepth -1)
        } else {
            throw LogicException("A slideable shouldn't be for multiple rectangulars")
        }
    }

    private fun containsTheIntersection(intersections: Set<DiagramElement>, r: RectAnchor)
        = intersections.firstOrNull { i -> r.e == i || r.e.deepContains(i) } != null

    private fun containsTheOrbit(orbits: Set<DiagramElement>, r: RectAnchor)
        = orbits.firstOrNull { o -> r.e.deepContains(o) } != null

    private fun hasHorizontalConstraint() : Boolean {
        return (direction != null) && Direction.isVertical(direction)
    }
    private fun hasVerticalConstraint() : Boolean {
        return (direction != null) && Direction.isHorizontal(direction)
    }

    private fun expensiveDirection(p: C2Point) : Boolean {
        return when (p.getPerp().dimension) {
            Dimension.V -> hasVerticalConstraint()
            Dimension.H -> hasHorizontalConstraint()
        }
    }

    override fun createInitialPaths(s: State<C2Route>) {
        start
            .filter { directionOk(it)}
            .forEach {
                // head out from each point as far as possible in each direction
                val along = it.getAlong()
                val perp = it.getPerp()
                val mrd1 = getMinimumRemainingDistance(perp)
                val mrd2 = getMinimumRemainingDistance(along)
                val initialDepth = allowedToLeave.get(this.startElem)!!
                val r = C2Route(null, it, C2Costing(initialDepth).addDistance(0, mrd1+mrd2, false))
                val d = r.point.d
                advance(d, perp, r, along, s, r.cost, true)
            }
    }

    private fun directionOk(p: C2Point) : Boolean {
        return if (direction != null) {
            p.d == direction
        } else {
            true
        }
    }

    companion object {

        fun isIncreasing(d: Direction): Boolean {
            return when (d) {
                Direction.UP, Direction.LEFT -> false
                Direction.DOWN, Direction.RIGHT -> true
            }
        }

    }
}