package org.kite9.diagram.visualization.compaction2.routing

import C2Costing
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.algorithms.ssp.AbstractSSP
import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import kotlin.math.abs

class C2SlideableSSP(
    val start: Set<C2Point>,
    val end: Set<C2Point>,
    private val startElem: DiagramElement,
    private val endElem: DiagramElement,
    private val endZone: Zone,
    private val direction: Direction?,
    private val junctions: Map<C2BufferSlideable, List<C2Slideable>>,
    private val hMatrix: Map<C2Slideable, Map<C2Slideable, Int>>,
    private val vMatrix: Map<C2Slideable, Map<C2Slideable, Int>>,
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

    private fun remainingDistance1D(s: C2Slideable, from: Int, to: Int) : Int {
        return if (s.maximumPosition!! < from) {
            from - s.maximumPosition!!
        } else if (s.minimumPosition > to) {
            s.minimumPosition - to
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
                    val dist = abs(k.minimumPosition - perp.minimumPosition)
                    val p = C2Point(along, k, d)
                    val travelledDistance = c.minimumTravelledDistance + dist
                    val possibleDistance = travelledDistance + remainingDistance(p)
                    val newCost = c.setDistances(travelledDistance, possibleDistance)
                    val r2 = C2Route(r, p, newCost)
                    s.add(r2)
                    log.send("Added: $r2")
                }
            }
        }
    }

    private fun getCorrectDistanceMatrix(from: C2Slideable, stops: List<C2Slideable>) : Map<C2Slideable, Int> {
        val mat = when (from.dimension) {
            Dimension.H -> hMatrix
            Dimension.V -> vMatrix
        }

        return mat.get(from)!!.filter { stops.contains(it.key) }
    }

    private fun blockingSlideable(k: C2Slideable): Boolean {
        return k is C2RectangularSlideable && k.anchors.size > 0
    }

    private fun collectInDirection(from: C2Slideable, forward: Boolean, stops: List<C2Slideable>) : Set<C2Slideable> {
        val stopsDistances = getCorrectDistanceMatrix(from, stops)

        // remove all the ones in the wrong direction
        val stopDistRightDirection = when (forward) {
            true -> stopsDistances.filter { it.value >= 0 }
            false -> stopsDistances.filter { it.value <= 0 }
        }.minus(from)

        // find nearest blocker
        val potentialBlockers = stopDistRightDirection.filter { blockingSlideable(it.key) }
        val closestBlocker = potentialBlockers.minByOrNull { abs(it.value) }

        if (closestBlocker != null) {
            val stopDistBeforeBlocker = stopDistRightDirection.filter { abs(it.value) <= abs(closestBlocker.value) }
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

        if (!intersectionOk) {
            return false
        }

        return when (k.dimension) {
            Dimension.H -> if (!hasHorizontalConstraint()) true else intersects(k, startPoint.get(k.dimension))
            Dimension.V -> if (!hasVerticalConstraint()) true else intersects(k, startPoint.get(k.dimension))
        }
    }

    private fun hasHorizontalConstraint() : Boolean {
        return (direction != null) && Direction.isVertical(direction)
    }
    private fun hasVerticalConstraint() : Boolean {
        return (direction != null) && Direction.isHorizontal(direction)
    }



    override fun createInitialPaths(s: State<C2Route>) {
        start
            .filter { directionOk(it)}
            .forEach {
            // head out from each point as far as possible in each direction
            val r = C2Route(null, it, C2Costing().setDistances(0, remainingDistance(it)))
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

        fun intersects(k1: C2Slideable, k2: C2Slideable) : Boolean {
            return intersects(k1.minimumPosition, k2.minimumPosition, k2.maximumPosition!!) ||
                    intersects(k1.maximumPosition!!, k2.minimumPosition, k2.maximumPosition!!)
        }

        private fun intersects(i1: Int, lower: Int, upper: Int): Boolean {
            return (i1 >= lower) && (i1<=upper)
        }
    }
}