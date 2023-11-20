package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.algorithms.ssp.AbstractSSP
import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction2.C2BufferSlideable
import org.kite9.diagram.visualization.compaction2.C2Slideable

class C2SlideableSSP(
    val start: Set<C2Point>,
    val end: Set<C2Point>,
    val allowTurns: Boolean,
    val log: Kite9Log
) : AbstractSSP<C2Route>() {
    override fun pathComplete(r: C2Route): Boolean {
        return end.contains(r.point)
    }

    override fun generateSuccessivePaths(r: C2Route, s: State<C2Route>) {
        val along = r.point.a  // along
        val perp = r.point.b  // arriving at
        val d = r.point.d
        log.send("Extending: ${r.cost} -> $r")

        advance(d, perp, r, along, s)

        if (allowTurns) {
            if ((along is C2BufferSlideable) && (perp is C2BufferSlideable)) {
                val dc = Direction.rotateClockwise(d)
                advance(dc, along, r, perp, s)

                val dac = Direction.rotateAntiClockwise(d)
                advance(dac, along, r, perp, s)
            }
        }
    }

    private fun advance(
        d: Direction,
        perp: C2Slideable,
        r: C2Route,
        along: C2Slideable,
        s: State<C2Route>
    ) {
        when (d) {
            Direction.RIGHT, Direction.DOWN -> perp.routesTo(true).forEach { (k, v) ->
                val r2 = C2Route(r, C2Point(along, k, d), r.cost + v)
                s.add(r2)
                log.send("Added: $r2")
            }

            Direction.LEFT, Direction.UP -> perp.routesTo(false).forEach { (k, v) ->
                val r2 = C2Route(r, C2Point(along, k, d), r.cost + v)
                s.add(r2)
                log.send("Added: $r2")
            }
        }
    }

    override fun createInitialPaths(s: State<C2Route>) {
        start.forEach { s.add(C2Route(null, it, 0)) }
    }

}