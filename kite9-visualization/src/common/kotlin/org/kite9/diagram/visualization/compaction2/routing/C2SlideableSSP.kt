package org.kite9.diagram.visualization.compaction2.routing

import C2Costing
import CostFreeTurn
import org.kite9.diagram.common.algorithms.ssp.AbstractSSP
import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.compaction2.Constraint
import kotlin.math.max

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

    val allowedToLeave : Set<DiagramElement> = run {
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

        out
    }

    override fun generateSuccessivePaths(r: C2Route, s: State<C2Route>) {
        val along = r.point.getAlong()
        val perp = r.point.getPerp()
        val d = r.point.d
        log.send("Extending: $r")

        // we might be able to remove this
        if (!along.isBlocker()) {
            advance(d, perp, r, along, s, r.cost.addStep())
        }

        if ((!along.isBlocker()) && (!perp.isBlocker())) {
            // turns ok if both axes are buffer slideable
            val dc = Direction.rotateClockwise(d)
            val nextScoreDc =  r.cost.addTurn(CostFreeTurn.CLOCKWISE)
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
        c: C2Costing
    ) {
        val r2 = canAdvancePast(perp, r, d)

        if (r2 !== null) {
            val leavers = getForwardSlideables(along, perp, d)
            leavers.forEach { k ->
                val p = C2Point(along, k, d)
                val travelledDistance = c.totalDistance + extraDistance(r2, p)
                val possibleRemainingDistance = getMinimumRemainingDistance(k)
                val expensive = expensiveDirection(p)
                val newCost = c.addDistance(travelledDistance, possibleRemainingDistance, expensive)
                val r3 = C2Route(r2, p, newCost)
                s.add(r3)
                log.send("Added: $r3")
            }
        }
    }

    enum class RelativeDirection { SAME, PERP, REVERSED }

    private fun extraDistance(existingRoute: C2Route?, newPoint: C2Point) : Int {
        if (existingRoute == null) {
            return 0
        }

        val routePoint = existingRoute.point
        val rd = when (newPoint.d) {
            routePoint.d -> RelativeDirection.SAME
            Direction.reverse(routePoint.d) -> RelativeDirection.REVERSED
            else -> RelativeDirection.PERP
        }

        val out = max(when (rd) {
            RelativeDirection.SAME -> getAbsoluteDistance(newPoint.getPerp(), routePoint.getPerp())
            RelativeDirection.PERP -> getAbsoluteDistance(newPoint.getPerp(), routePoint.getAlong())
            RelativeDirection.REVERSED -> 0
        }, extraDistance(existingRoute.prev, newPoint))

        return out
    }

    fun getAbsoluteDistance(from: C2Slideable, to: C2Slideable): Int {
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
        return known + unknown
    }

    private fun hasRoutes(b: C2Slideable) : Boolean {
        val out = b.getConnAnchors().isNotEmpty()
        return out
    }

    private fun getBlockingIntersections(intersections: Set<C2Slideable>) : Set<C2Slideable> {
        val out =  intersections.filter {
            hasRoutes(it) || it.isBlocker()
        }.toSet()

        return out
    }

    private fun collectInDirection(from: C2Slideable, forward: Boolean, intersections: Set<C2Slideable>) : Set<C2Slideable> {
        val stopsDistances = getCorrectDistanceMatrix(from, intersections)

        // remove all the ones in the wrong direction
        val stopDistRightDirection = stopsDistances.filter { it.value == null || it.value!!.forward == forward }
            .minus(from)

        val potentialBlockers = getBlockingIntersections(intersections)
            .map { it to stopDistRightDirection[it] }
            .filter { (_, t) -> t != null }
            .associate { (f, t) -> f to t!! }

        val closestBlocker = potentialBlockers.minByOrNull { it.value.dist }

        if (closestBlocker != null) {
            val stopDistBeforeBlocker = stopDistRightDirection.filter { it.value == null || it.value!!.dist <= closestBlocker.value.dist }
            return stopDistBeforeBlocker.keys + closestBlocker.key
        }

        return stopDistRightDirection.keys
    }

    private fun getForwardSlideables(along: C2Slideable, startingAt: C2Slideable, going: Direction) : Set<C2Slideable> {
        val forward = when(going) {
            Direction.DOWN, Direction.RIGHT -> true
            Direction.LEFT, Direction.UP -> false
        }
        val out = collectInDirection(startingAt, forward, c2.getIntersections(along) ?: emptySet())
        return out
    }

    /**
     * Cost of crossing over another edge
     */
    private fun addCrossCost(r: C2Route, cbs: C2Slideable) : C2Route {
        val isCrossing = cbs.getRectangulars().isNotEmpty() || cbs.getConnAnchors().isNotEmpty()

        return if (isCrossing) {
            C2Route(r, r.point, r.cost.addCrossing(true))
        } else {
            r
        }
    }

    private fun canAdvancePast(perp: C2Slideable, routeIn: C2Route, d: Direction): C2Route? {
        // this is approximate - might need improvement later
        //val isIntersection = perp.intersecting().isNotEmpty()
        val isRectangular = perp.isBlocker()
        val isOrbit = perp.getOrbits().isNotEmpty()

        if (isRectangular) {
            // we can only cross a rectangular slideable if it's a container
            val okAnchorDirection = if (isIncreasing(d)) Side.END else Side.START
            val matchingAnchors = perp.getRectangulars()
                .any {
                    (it.s == okAnchorDirection ) || (allowedToLeave.contains(it.e))
                }
            if (!matchingAnchors) {
                log.send("Can't move on from $perp going $d")
                return null
            }
            return addCrossCost(routeIn, perp)
        }

        if (isOrbit) {
            return addCrossCost(routeIn, perp)
        }

        return routeIn
    }


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
            val r = C2Route(null, it, C2Costing().addDistance(0, mrd1+mrd2, false))
            val d = r.point.d
            advance(d, perp, r, along, s, r.cost)
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

//        fun parents(x: DiagramElement?) : Set<DiagramElement> {
//            return if (x == null) {
//                setOf()
//            } else {
//                return setOf(x).plus(parents(x.getParent()))
//            }
//        }

        fun isIncreasing(d: Direction): Boolean {
            return when (d) {
                Direction.UP, Direction.LEFT -> false
                Direction.DOWN, Direction.RIGHT -> true
            }
        }

//        private fun intersects(i1: Int, lower: Int, upper: Int): Boolean {
//            return (i1 >= lower) && (i1<=upper)
//        }
    }
}