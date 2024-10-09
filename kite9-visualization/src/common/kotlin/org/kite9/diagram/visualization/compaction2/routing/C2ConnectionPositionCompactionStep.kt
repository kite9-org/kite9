package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Terminator
import org.kite9.diagram.model.position.BasicDimension2D
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.RouteRenderingInformation
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.anchors.ConnAnchor
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import kotlin.math.max

/**
 * Decorates the compaction process to handle Connections which have RouteRenderingInformation.
 *
 * Needs to be done after the positions of all edges have been set.
 *
 * @author robmoffat
 */
class C2ConnectionPositionCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override val prefix: String
        get() = "CPOS"

    override val isLoggingEnabled: Boolean
        get() = true

    override fun compact(c: C2Compaction, g: Group) {
        val routes = buildRoutes(c)
        routes
            .forEach { (k, v) -> setRoute(k, v) }
    }

    private fun create1DRouteMap(cso: C2SlackOptimisation) : Map<Connection,Map<Int, C2Slideable>> {
        cso.checkConsistency()
        val out = mutableMapOf<Connection, MutableMap<Int, C2Slideable>>()

        cso.getAllSlideables()
            .forEach { rs -> rs.anchors
                .filterIsInstance<ConnAnchor>()
                .filter { it.e.getRenderingInformation().rendered }
                .forEach { a ->
                    val conn = a.e
                    val idx = a.s
                    val map = out.getOrPut(conn) { mutableMapOf() }
                    map[idx] = rs
                }
        }

        return out
    }

    private fun <A> zipMapsToList(h: Map<Int, A>, v: Map<Int, A>) : List<Pair<A, A>> {
        val m = max(h.keys.max(), v.keys.max())
        val out = (0..m)
            .map { h[it]!! to v[it]!! }
        return out.reversed()
    }

    private fun buildRoutes(c: C2Compaction) : Map<Connection, List<Pair<C2Slideable, C2Slideable>>> {
        val hs = c.getSlackOptimisation(Dimension.H);
        val vs = c.getSlackOptimisation(Dimension.V)

        val hsMap = create1DRouteMap(hs)
        val vsMap = create1DRouteMap(vs)

        return hsMap.entries.associate { e ->
            val h = e.value
            val v = vsMap[e.key]!!
            e.key to zipMapsToList(h, v)
        }
    }


    private fun setRoute(tle: Connection, points: List<Pair<C2Slideable, C2Slideable>>) {
        val out = tle.getRenderingInformation()
        out.clear()
        var first = true
        var last: Dimension2D? = null
        for (v in points) {
            val next = addToRoute(out, v, last)
            setPortPosition(v, next)
            if (first && last != null && next != null) {
                setTerminatorPositionAndOrientation(tle.getFromDecoration(), last)
                first = false
            }
            if (next != null) {
                last = next
            }
        }
        if (last != null) {
            setTerminatorPositionAndOrientation(tle.getToDecoration(), last)
        }
    }

    private fun setPortPosition(v: Pair<C2Slideable, C2Slideable>, d2: Dimension2D?) {
//        if (d2 == null) {
//            return
//        }
//        v.getDiagramElements()
//            .filterIsInstance<Port>()
//            .forEach {
//                it.getRenderingInformation().position = CostedDimension2D(d2.x(), d2.y())
//                it.getRenderingInformation().size = CostedDimension2D(0.0, 0.0)
//            }
    }

    private fun addToRoute(out: RouteRenderingInformation, p: Pair<C2Slideable, C2Slideable>, prev: Dimension2D?): Dimension2D? {
        val horizSeg = p.first
        val vertSeg = p.second
        val y2 = vertSeg.minimumPosition.toDouble()
        val x2 = horizSeg.minimumPosition.toDouble()
        val p1: Dimension2D = BasicDimension2D(x2, y2)
        if (prev != null) {
            if (prev.x() == p1.x() && prev.y() == p1.y()) {
                return null // prevent duplicates in the list
            }
        }
        out.add(p1)
        return p1
    }

    private fun setTerminatorPositionAndOrientation(t: Terminator?, pos: Dimension2D) {
        if (t == null) {
            return
        }

        val x1 = pos.x()
        val y1 = pos.y()
        val rri = t.getRenderingInformation()
        rri.position = BasicDimension2D(x1, y1)
        rri.size = BasicDimension2D(0.0, 0.0)
    }

}