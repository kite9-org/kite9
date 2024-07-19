package org.kite9.diagram.visualization.compaction2.routing

import C2Costing
import org.kite9.diagram.common.algorithms.ssp.AbstractSSP
import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import kotlin.math.abs
import kotlin.math.max

class C2SlideableSSP(
    val start: Set<C2Point>,
    val end: Set<C2Point>,
    private val startElem: DiagramElement,
    private val endElem: DiagramElement,
    private val endZone: Zone,
    private val direction: Direction?,
    private val junctions: Map<C2BufferSlideable, List<C2Slideable>>,
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

    val allowedTraversal : Set<DiagramElement> = run {
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

    private fun remainingDistance1D(s: C2Slideable, from: C2Slideable, to: C2Slideable) : Int {
        // first, let's see if there's min distance set
        val knownDistance = getCorrectDistanceMatrix(s, listOf(from, to)).values
            .filterNotNull()
            .maxOfOrNull { it.dist }

        return if (knownDistance != null) {
            knownDistance
        } else if (s.maximumPosition!! < from.minimumPosition) {
            from.minimumPosition - s.maximumPosition!!
        } else if (s.minimumPosition > to.maximumPosition!!) {
            s.minimumPosition - to.maximumPosition!!
        } else {
            0
        }

    }

    private fun remainingDistance(p: C2Point) : Int {
        val h = p.get(Dimension.H)
        val v = p.get(Dimension.V)
        return remainingDistance1D(h, endZone.minY, endZone.maxY) +
                remainingDistance1D(v, endZone.minX, endZone.maxX)
    }

    override fun generateSuccessivePaths(r: C2Route, s: State<C2Route>) {
        val along = r.point.getAlong()
        val perp = r.point.getPerp()
        val d = r.point.d
        log.send("Extending: $r")

        if (along is C2BufferSlideable) {
            advance(d, perp, r, along, s, r.cost.addStep())
        }

        if ((along is C2BufferSlideable) && (perp is C2BufferSlideable)) {
            // turns ok if both axes are buffer slideable
            val dc = Direction.rotateClockwise(d)
            advance(dc, along, r, perp, s, r.cost.addTurn())

            val dac = Direction.rotateAntiClockwise(d)
            advance(dac, along, r, perp, s, r.cost.addTurn())
        }

    }


    private val commonMap = mutableMapOf<C2IntersectionSlideable, Set<DiagramElement>>()

    private fun includeParentsOf(s: C2IntersectionSlideable) : Set<DiagramElement> {

        return commonMap.getOrPut(s) {
            s.intersects.flatMap { parents(it) }.toSet()
        }
    }

    private fun advance(
        d: Direction,
        perp: C2Slideable,
        r: C2Route,
        along: C2BufferSlideable,
        s: State<C2Route>,
        c: C2Costing
    ) {
        val common = when (along) {
            is C2IntersectionSlideable -> includeParentsOf(along)
            is C2OrbitSlideable -> along.orbits.map { it.e }.toSet()
        }

        val startPoint = C2ConnectionRouterCompactionStep.getStart(r)

        if (canAdvanceFrom(perp, d, common, along)) {
            val leavers = getForwardSlideables(along, perp, d)
            leavers.forEach { k ->
                if (canAdvanceTo(common, k, startPoint)) {
                    val p = C2Point(along, k, d)
                    val travelledDistance = c.totalDistance + extraDistance(r, p)
                    val possibleRemainingDistance = remainingDistance(p)
                    val expensive = expensiveDirection(p)
                    val newCost = c.addDistance(travelledDistance, possibleRemainingDistance, expensive)
                    val r2 = C2Route(r, p, newCost)
                    s.add(r2)
                    log.send("Added: $r2")
                }
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

    private fun getCorrectDistanceMatrix(from: C2Slideable, stops: List<C2Slideable>) : Map<C2Slideable, Constraint?> {
        val mat = when (from.dimension) {
            Dimension.H -> hMatrix
            Dimension.V -> vMatrix
        }

        val distances = mat.get(from)!!
        val known = distances.filter { stops.contains(it.key) }
        val unknown = stops.filter { !distances.containsKey(it) }.associateWith { null }
        return known + unknown
    }

    private fun blockingSlideable(k: Map.Entry<C2Slideable, Constraint?>): Boolean {
        // this should also block at another link
        return (k.key is C2RectangularSlideable) &&
                ((k.key as C2RectangularSlideable).anchors.size > 0)
    }

    private fun collectInDirection(from: C2Slideable, forward: Boolean, stops: List<C2Slideable>) : Set<C2Slideable> {
        val stopsDistances = getCorrectDistanceMatrix(from, stops)

        // remove all the ones in the wrong direction
        val stopDistRightDirection = stopsDistances.filter { it.value == null || it.value!!.forward == forward }
            .minus(from)

        // find nearest blocker
        val potentialBlockers = stopDistRightDirection
            .filter { blockingSlideable(it) }
            .filterValues { it != null } as Map<C2Slideable, Constraint>

        val closestBlocker = potentialBlockers.minByOrNull { it.value.dist }

        if (closestBlocker != null) {
            val stopDistBeforeBlocker = stopDistRightDirection.filter { it.value == null || it.value!!.dist <= closestBlocker.value.dist }
            return stopDistBeforeBlocker.keys
        }

        return stopDistRightDirection.keys
    }

    private fun getForwardSlideables(along: C2BufferSlideable, startingAt: C2Slideable, going: Direction) : Set<C2Slideable> {
        val stops = junctions[along]!!

        val forward = when(going) {
            Direction.DOWN, Direction.RIGHT -> true
            Direction.LEFT, Direction.UP -> false
        }

        val out = collectInDirection(startingAt, forward, stops)
        return out
    }

    private fun canAdvanceFrom(perp: C2Slideable, d: Direction, common: Set<DiagramElement>, along: C2BufferSlideable): Boolean {
        return when (perp) {
            is C2BufferSlideable -> true
            is C2RectangularSlideable -> {
                if (along is C2IntersectionSlideable) {
                    val okAnchorDirection = if (isIncreasing(d)) Side.END else Side.START
                    val matchingAnchors = perp.anchors
                        .filter { common.contains(it.e) }
                        .any {
                            (it.s == okAnchorDirection ) || (allowedTraversal.contains(it.e))
                        }
                    if (!matchingAnchors) {
                        log.send("Can't move on from $perp going $d")
                    }
                    return matchingAnchors
                } else {
                    return true
                }
            }
        }
    }


    /**
     * This looks at a slideable and says whether we can go to it, returning the cost of
     * doing so if so.
     */
    private fun canAdvanceTo(common: Set<DiagramElement>, k: C2Slideable, startPoint: C2Point): Boolean {
        val intersectionOk = when (k) {
            is C2OrbitSlideable ->  k.orbits.any { common.contains(it.e) }
            is C2IntersectionSlideable -> k.intersects.any { it == endElem || common.contains(it) }
            is C2RectangularSlideable -> k.anchors.any { common.contains(it.e) }
        }

        return intersectionOk
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
            val r = C2Route(null, it, C2Costing().addDistance(0, remainingDistance(it), false))
            val along = r.point.getAlong() as C2BufferSlideable
            val perp = r.point.getPerp()
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

        fun parents(x: DiagramElement?) : Set<DiagramElement> {
            return if (x == null) {
                setOf()
            } else {
                return setOf(x).plus(parents(x.getParent()))
            }
        }

        fun isIncreasing(d: Direction): Boolean {
            return when (d) {
                Direction.UP, Direction.LEFT -> false
                Direction.DOWN, Direction.RIGHT -> true
            }
        }

        private fun intersects(i1: Int, lower: Int, upper: Int): Boolean {
            return (i1 >= lower) && (i1<=upper)
        }
    }
}