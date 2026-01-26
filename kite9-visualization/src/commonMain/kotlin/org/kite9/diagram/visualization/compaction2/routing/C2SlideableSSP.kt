package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.algorithms.ssp.AbstractSSP
import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.compaction2.anchors.RectAnchor
import kotlin.math.abs

class C2SlideableSSP(
    val e: Connection,
    val start: Set<C2Point>,
    val end: Set<C2Point>,
    private val startElem: ConnectedRectangular,
    private val endElem: ConnectedRectangular,
    private val direction: Direction?,
    private val c2: C2Compaction,
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
        var se : DiagramElement = startElem
        var ee : DiagramElement = endElem
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
        val leavers = nextInDirection(perp, d, along)
        val out = leavers
            .filter { k -> r.coords.isInBounds(k, d) }
            .map { k ->
                val p = C2Point(along, k, d)
                val stride = r.coords.distanceTo(p)
                val possibleRemainingDistance = getMinimumRemainingDistance(k)
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
                    val route2 = crossThreshold(perp, r, d, r.cost, along)
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
        return abs(to.minimumPosition - from.minimumPosition)
    }

    private fun getMinimumRemainingDistance(from: C2Slideable): Int {
        return end.map { it.get(from.dimension) }
            .map { getAbsoluteDistance(it, from) }
            .minOfOrNull { it } ?: 0
    }

    private fun nextInDirection(from: C2Slideable,  d: Direction, along: C2Slideable) : Set<C2Slideable> {
        fun positionBefore(a: C2Slideable, b: C2Slideable) : Boolean {
            return a.minimumPosition < b.minimumPosition
        }

        fun positionSame(a: C2Slideable, b: C2Slideable) : Boolean {
            return a.minimumPosition  == b.minimumPosition
        }

        fun positionAfter(a: C2Slideable, b: C2Slideable) : Boolean {
            return a.minimumPosition > b.minimumPosition
        }

        fun numericallyBefore(a: C2Slideable, b: C2Slideable) : Boolean {
            return a.number < b.number
        }

        fun numericallyAfter(a: C2Slideable, b: C2Slideable) : Boolean {
            return a.number > b.number
        }

        val neighbours = this.c2.getNeighbours(along, from)

        val neighboursInRightDirection = when (d) {
            Direction.UP, Direction.LEFT ->
                neighbours.filter { positionBefore(it, from) || (positionSame(it, from) && numericallyBefore(it, from)) }
            Direction.RIGHT, Direction.DOWN ->
                neighbours.filter { positionAfter(it, from) || (positionSame(it, from) && numericallyAfter(it, from)) }
        }

        val minDistance = neighboursInRightDirection.map { getAbsoluteDistance(from, it) }.minOfOrNull { it }

        if (minDistance != null) {
            val neighboursMinDistance =
                neighboursInRightDirection.filter { getAbsoluteDistance(from, it) == minDistance }
            return neighboursMinDistance.toSet()
        } else {
            return emptySet()
        }
    }

    private fun isGridCell(e: DiagramElement) : Boolean {
        return (e.getParent() as Container)?.getLayout() == Layout.GRID
    }

    private fun crossThreshold(perp: C2Slideable, routeIn: C2Route, d: Direction, c: C2Costing, along: C2Slideable): C2Route? {
        val a = perp.getRelevantRectAnchor(routeIn.container)

        if (a == null) {
            if (routeIn.container is Diagram) {
                // trying to leave the diagram
                return null
            } else {
                // crossing grid cells
                return routeIn
            }
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