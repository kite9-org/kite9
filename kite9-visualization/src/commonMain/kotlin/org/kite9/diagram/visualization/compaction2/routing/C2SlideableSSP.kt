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
import org.kite9.diagram.visualization.compaction2.Location
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

    fun isDestination(ra: Set<RectAnchor>) : Boolean {
        return ra.find { it.e == endElem } != null
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
        advance(d, perp, r.updateCost(r.cost.addStep()), along, s)

        if (perp.getRectAnchors().isEmpty()) {
            val dc = Direction.rotateClockwise(d)
            val nextScoreDc = r.cost.addTurn(CostFreeTurn.CLOCKWISE)
            advance(dc, along, r.updateCost(nextScoreDc), perp, s)

            val dac = Direction.rotateAntiClockwise(d)
            val nextScoreDac = r.cost.addTurn(CostFreeTurn.ANTICLOCKWISE)
            advance(dac, along, r.updateCost(nextScoreDac), perp, s)
        }
    }

    private fun generateNextSteps( d: Direction,
                                   perp: C2Slideable,
                                   r: C2Route,
                                   along: C2Slideable) : List<C2Route> {
        val leavers = getForwardSlideables(along, perp, d)
        val out = leavers
            .filter { k -> r.coords.isInBounds(k.key, d) }
            .map { k ->
                val p = C2Point(along, k.key, d)
                val stride = r.coords.distanceTo(p)
                val possibleRemainingDistance = getMinimumRemainingDistance(k.key)
                val expensive = expensiveDirection(p)
                val newCost = r.cost.addDistance(stride, possibleRemainingDistance, expensive)
                r.advance(p, newCost)
            }

        return out
    }

    private fun advance(
        d: Direction,
        perp: C2Slideable,
        r: C2Route,
        along: C2Slideable,
        s: State<C2Route>
    ) {
        if (along.canMoveAlongInside(r.container)) {
            val out1 = generateNextSteps(d, perp, r, along)
            val out2 = out1.flatMap { r ->
                val along = r.point.getAlong()
                val perp = r.point.getPerp()
                val d = r.point.d
                val ra = perp.getRectAnchors()
                if (ra.isNotEmpty() && !isDestination(ra)) {
                    // if we arrive at a rectangular, we need to cross it
                    val route2 = crossThreshold(perp, r, d, r.cost)
                    if (route2 != null) {
                        val ns = generateNextSteps(d, perp, route2, along)
                        ns
                    } else {
                        emptyList()
                    }
                } else {
                    listOf(r)
                }
            }
            out2.forEach {
                if (s.add(it)) {
                    log.send("Added: $it")
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

        val dimension = along.dimension
        val location = if (dimension == Dimension.H) {
            Location(along, startingAt)
        } else {
            Location(startingAt, along)
        }

        val furtherPoints = c2.getNeighbours(location, dimension)
        val out = collectInDirection(startingAt, forward, furtherPoints, going, along)
        return out
    }

    private fun crossThreshold(perp: C2Slideable, routeIn: C2Route, d: Direction, c: C2Costing): C2Route? {
        val a = perp.getRelevantRectAnchor(routeIn.container)

        if (a == null) {
            // trying to leave the diagram
            return null
        } else if (a.canCross(d)) {
            val entering = a.s.isEntering(d);
            val newContainer = if (!entering) a.e.getContainer() else a.e
            if (newContainer == null) {
                return null
            } else {
                val oldDepth = routeIn.cost.containerDepth
                val newDepth = if (entering) oldDepth+1 else oldDepth -1
                val newCost = c.addCrossing(true, newDepth)
                val out = routeIn.changeContainer(newContainer, newCost)
                return out
            }

        } else {
            return null;
        }
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
                val initialDepth = allowedToLeave.get(this.startElem)!!
                val initialContainer = this.startElem.getParent()!!
                val initialCoords = C2Coords.createInitialCoords(it)
                val initialCost = C2Costing(initialDepth).addDistance(0, mrd1+mrd2, false)
                val r = C2Route(null, initialCoords, it, initialCost, initialContainer)
                val d = r.point.d
                advance(d, perp, r, along, s)
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