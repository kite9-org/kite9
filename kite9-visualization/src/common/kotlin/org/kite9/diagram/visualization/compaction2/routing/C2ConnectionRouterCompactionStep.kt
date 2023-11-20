package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.algorithms.ssp.NoFurtherPathException
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Port
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.links.RankBasedConnectionQueue
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D


class C2ConnectionRouterCompactionStep(cd: CompleteDisplayer, r: GroupResult) : AbstractC2ContainerCompactionStep(cd,r) {

    override val prefix = "C2CR"
    override val isLoggingEnabled = true

    val usedStartEndPoints = mutableSetOf<C2Point>()

    private fun createPoints(c2: C2Compaction, d: Connected, arriving: Boolean) : Set<C2Point> {
        val h = c2.getSlackOptimisation(Dimension.H)
        val v = c2.getSlackOptimisation(Dimension.V)
        val hss = h.getSlideablesFor(d)!!
        val vss = v.getSlideablesFor(d)!!

        return setOf(
            C2Point(hss.c, vss.l, if (arriving) Direction.DOWN else Direction.UP),  // top
            C2Point(hss.c, vss.r, if (arriving) Direction.UP else Direction.DOWN),  // bottom
            C2Point(vss.c, hss.l, if (arriving) Direction.RIGHT else Direction.LEFT),  // left
            C2Point(vss.c, hss.r, if (arriving) Direction.LEFT else Direction.RIGHT)   // right
        ).minus(usedStartEndPoints)
    }

    private fun insertLink(c2: C2Compaction, c: Connection) {
        try {
            val startingPoints = createPoints(c2, c.getFrom(), false)
            val endingPoints = createPoints(c2, c.getTo(), true)
            val allowTurns = (c.getDrawDirection() == null) || (c.getRenderingInformation().isContradicting)

            val doer = C2SlideableSSP(startingPoints, endingPoints, allowTurns, log)
            log.send("Routing: $c")
            val out = doer.createShortestPath()

            log.send("Found shortest path: $out")

            updateUsedStartEndPoints(out.point, c.getTo())
            updateUsedStartEndPoints(getStart(out), c.getFrom())


            c2.getRoutes()[c] = out

        } catch (e: NoFurtherPathException) {
            log.error("Couldn't route: $c")
        }
    }

    private fun getStart(out: C2Route) : C2Point {
        return if (out.prev != null) {
            getStart(out.prev)
        } else {
            out.point
        }
    }

    private fun updateUsedStartEndPoints(out: C2Point, from: Connected) {
        if (from is Port) {
            // ports are allowed to have multiple connections
        } else {
            usedStartEndPoints.add(out)
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