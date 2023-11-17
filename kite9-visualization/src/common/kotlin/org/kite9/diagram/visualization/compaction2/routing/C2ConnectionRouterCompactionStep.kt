package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.algorithms.ssp.AbstractSSP
import org.kite9.diagram.common.algorithms.ssp.NoFurtherPathException
import org.kite9.diagram.common.algorithms.ssp.State
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.links.RankBasedConnectionQueue
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D

class C2SlideableSSP(
    val start: Set<C2Point>,
    val end: Set<C2Point>,
    val log: Kite9Log) : AbstractSSP<C2Route>() {
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
                val r2 = C2Route(r, C2Point(d1, k, d), r.cost+v)
                s.add(r2)
                log.send("Added: $r2")
            }
            Direction.LEFT, Direction.UP  -> d2.routesTo(false).forEach { (k, v) ->
                val r2 = C2Route(r, C2Point(d1, k, d), r.cost+v)
                s.add(r2)
                log.send("Added: $r2")
            }
        }
    }

    override fun createInitialPaths(s: State<C2Route>) {
        start.forEach { s.add(C2Route(null, it, 0)) }
    }

}


class C2ConnectionRouterCompactionStep(cd: CompleteDisplayer, r: GroupResult) : AbstractC2ContainerCompactionStep(cd,r) {

    override val prefix = "C2CR"
    override val isLoggingEnabled = true


    private fun createPoints(c2: C2Compaction, d: Connected, arriving: Boolean) : Set<C2Point> {
        val h = c2.getSlackOptimisation(Dimension.H)
        val v = c2.getSlackOptimisation(Dimension.V)
        val hss = h.getSlideablesFor(d)!!
        val vss = v.getSlideablesFor(d)!!

        if (arriving) {
            return setOf(
                C2Point(hss.c, vss.l, if (arriving) Direction.DOWN else Direction.UP),  // top
                C2Point(hss.c, vss.r, if (arriving) Direction.UP else Direction.DOWN),  // bottom
                C2Point(vss.c, hss.l, if (arriving) Direction.LEFT else Direction.RIGHT),  // left
                C2Point(vss.c, hss.r, if (arriving) Direction.RIGHT else Direction.LEFT)   // right
            )
        } else {
            return setOf(
                //  Point(hss.c, vss.l, if (arriving) Direction.DOWN else Direction.UP),  // top
                //  Point(hss.c, vss.r, if (arriving) Direction.UP else Direction.DOWN),  // bottom
                //Point(vss.c, hss.l, if (arriving) Direction.LEFT else Direction.RIGHT),  // left
                C2Point(vss.c, hss.r, if (arriving) Direction.RIGHT else Direction.LEFT)   // right
            )
        }

//        return setOf(
//          //  Point(hss.c, vss.l, if (arriving) Direction.DOWN else Direction.UP),  // top
//          //  Point(hss.c, vss.r, if (arriving) Direction.UP else Direction.DOWN),  // bottom
//            Point(vss.c, hss.l, if (arriving) Direction.LEFT else Direction.RIGHT),  // left
//            Point(vss.c, hss.r, if (arriving) Direction.RIGHT else Direction.LEFT)   // right
//        )
    }

    private fun insertLink(c2: C2Compaction, c: Connection) {
        try {
            val startingPoints = createPoints(c2, c.getFrom(), false)
            val endingPoints = createPoints(c2, c.getTo(), true)

            val doer = C2SlideableSSP(startingPoints, endingPoints, log)
            log.send("Routing: $c")
            val out = doer.createShortestPath()

            log.send("Found shortest path: $out")

            c2.getRoutes()[c] = out

        } catch (e: NoFurtherPathException) {
            log.error("Couldn't route: $c")
        }
    }

    private fun buildQueue(g: Group, queue: RankBasedConnectionQueue) {
        if (g is CompoundGroup) {
            buildQueue(g.a, queue)
            buildQueue(g.b, queue)
        }

        queue.handleLinks(g)
    }

    override fun compact(c: C2Compaction, g: Group) {
        val queue = RankBasedConnectionQueue(PositionRoutableHandler2D())
        buildQueue(g, queue)

        val vso = c.getSlackOptimisation(Dimension.V)
        val hso = c.getSlackOptimisation(Dimension.H)

        visitRectangulars(c.getDiagram()) {
            ensureCentreSlideablePosition(hso, it)
            ensureCentreSlideablePosition(vso, it)
        }

        queue.forEach {
            if (it is Connection) {
                insertLink(c, it)
            }
        }

    }
}