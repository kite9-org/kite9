package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Terminator
import org.kite9.diagram.model.position.BasicDimension2D
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.RouteRenderingInformation
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

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
        c.getRoutes()
            .filter { (k) -> k.getRenderingInformation().rendered }
            .forEach { (k, v) -> setRoute(k, v) }
    }


    private fun setRoute(tle: Connection, route: C2Route) {
        val points = routeToPoints(route)
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

    private fun routeToPoints(route: C2Route): List<C2Point> {
        return if (route.prev == null) {
            listOf(route.point)
        } else {
            routeToPoints(route.prev).plus(route.point)
        }
    }

    private fun setPortPosition(v: C2Point, d2: Dimension2D?) {
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

    private fun addToRoute(out: RouteRenderingInformation, p: C2Point, prev: Dimension2D?): Dimension2D? {
        val horizSeg = p.get(Dimension.H)
        val vertSeg = p.get(Dimension.V)
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