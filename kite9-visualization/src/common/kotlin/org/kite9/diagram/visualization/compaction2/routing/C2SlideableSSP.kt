package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.algorithms.ssp.AbstractSSP
import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.position.Direction

class C2SlideableSSP(
    val start: Set<C2Point>,
    val end: Set<C2Point>,
    val log: Kite9Log
) : AbstractSSP<C2Route>() {
    override fun pathComplete(r: C2Route): Boolean {
        return end.contains(r.point)
    }

    override fun generateSuccessivePaths(r: C2Route, s: State<C2Route>) {
        val d1 = r.point.a  // along
        val d2 = r.point.b  // arriving at
        val d = r.point.d
        log.send("Extending: ${r.cost} -> $r")

        when (d) {
            Direction.RIGHT, Direction.DOWN -> d2.routesTo(true).forEach { (k, v) ->
                val r2 = C2Route(r, C2Point(d1, k, d), r.cost + v)
                s.add(r2)
                log.send("Added: $r2")
            }
            Direction.LEFT, Direction.UP -> d2.routesTo(false).forEach { (k, v) ->
                val r2 = C2Route(r, C2Point(d1, k, d), r.cost + v)
                s.add(r2)
                log.send("Added: $r2")
            }
        }
    }

    override fun createInitialPaths(s: State<C2Route>) {
        start.forEach { s.add(C2Route(null, it, 0)) }
    }

}