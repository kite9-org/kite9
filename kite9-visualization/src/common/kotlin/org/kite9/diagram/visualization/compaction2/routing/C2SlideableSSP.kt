package org.kite9.diagram.visualization.compaction2.routing

import C2Costing
import org.kite9.diagram.common.algorithms.ssp.AbstractSSP
import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.BufferType
import org.kite9.diagram.visualization.compaction2.C2BufferSlideable
import org.kite9.diagram.visualization.compaction2.C2RectangularSlideable
import org.kite9.diagram.visualization.compaction2.C2Slideable
import kotlin.math.abs

data class Zone(val minX: Int, val maxX: Int, val minY: Int, val maxY: Int)

class C2SlideableSSP(
    val start: Set<C2Point>,
    val end: Set<C2Point>,
    private val startElem: DiagramElement,
    private val endElem: DiagramElement,
    private val endZone: Zone,
    private val allowTurns: Boolean,
    val log: Kite9Log
) : AbstractSSP<C2Route>() {
    override fun pathComplete(r: C2Route): Boolean {
        return end.contains(r.point)
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

        if (allowTurns) {
            if ((along is C2BufferSlideable)
                && (perp is C2BufferSlideable)) {
                // turns ok if both axes are buffer slideable
                val dc = Direction.rotateClockwise(d)
                advance(dc, along, r, perp, s, r.cost.addTurn())

                val dac = Direction.rotateAntiClockwise(d)
                advance(dac, along, r, perp, s, r.cost.addTurn())
            }
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
        val common = when (along.getBufferType()) {
            BufferType.INTESECTER -> setOf(startElem, endElem)
            BufferType.ORBITER -> along.orbits
        }

        if (canAdvanceFrom(perp, d, common, along)) {
            val leavers = perp.routesTo(isIncreasing(d))
            leavers.forEach { (k, v) ->
                if (canAdvanceTo(common, k)) {
                    val p = C2Point(along, k, d)
                    val travelledDistance = c.minimumTravelledDistance + v
                    val possibleDistance = travelledDistance + remainingDistance(p)
                    val newCost = c.setDistances(travelledDistance, possibleDistance)
                    val r2 = C2Route(r, p, newCost)
                    s.add(r2)
                    log.send("Added: $r2")
                }
            }
        }
    }

    private fun canAdvanceFrom(perp: C2Slideable, d: Direction, common: Set<DiagramElement>, along: C2BufferSlideable): Boolean {
        return when (perp) {
            is C2BufferSlideable -> true
            is C2RectangularSlideable -> {
                if (along.getBufferType() == BufferType.INTESECTER) {
                    val okAnchorDirection = if (isIncreasing(d)) Side.END else Side.START
                    val matchingAnchors = perp.anchors
                        .filter { common.contains(it.e) }
                        .any { it.s == okAnchorDirection }
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
     * THis looks at a slideable and says whether we can go to it, returning the cost of
     * doing so if so.
     */
    private fun canAdvanceTo(common: Set<DiagramElement>, k: C2Slideable): Boolean {
        return when (k) {
            is C2RectangularSlideable -> k.anchors.any { common.contains(it.e) }
            is C2BufferSlideable -> if (k.getBufferType() == BufferType.ORBITER) {
                k.orbits.any { common.contains(it) }
            } else {
                nextTo(k.intersects, common, endElem)
            }
        }
    }

    private fun <K> nextTo(l: List<K>, a: Set<K>, b: K) : Boolean {
        val i2 = l.indexOf(b)
        val closest = a.map { l.indexOf(it) }
            .filter { it != -1 }.minOfOrNull { abs(i2 - it) }

        return (closest != null) && (closest < 2)
    }

    override fun createInitialPaths(s: State<C2Route>) {
        start.forEach {
            // head out from each point as far as possible in each direction
            val r = C2Route(null, it, C2Costing().setDistances(0, remainingDistance(it)))
            val along = r.point.getAlong() as C2BufferSlideable
            val perp = r.point.getPerp()
            val d = r.point.d
            advance(d, perp, r, along, s, r.cost)
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