package org.kite9.diagram.visualization.compaction.position

import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Port
import org.kite9.diagram.model.Terminator
import org.kite9.diagram.model.position.*
import org.kite9.diagram.model.position.Direction.Companion.rotateClockwise
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.CompactionStep
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization

/**
 * Decorates the compaction process to handle Connections which have RouteRenderingInformation.
 *
 * Needs to be done after the positions of all edges have been set.
 *
 * @author robmoffat
 */
class ConnectionRouteCompactionStep : CompactionStep {

    override fun compact(c: Compaction, r: Embedding, cr: Compactor) {
        if (r.isTopEmbedding) {
            val renderedConnections = createTopElementSet(c.getOrthogonalization())
            for (de in renderedConnections) {
                setRoute(de, c.getOrthogonalization().getWaypointsForBiDirectional(de), c)
            }
        }
    }

    private fun createTopElementSet(c: Orthogonalization): Set<Connection> {
        val out: MutableSet<Connection> = LinkedHashSet()
        for (e in c.getAllDarts()) {
            for (de in e.getDiagramElements().keys) {
                if (de is Connection) {
                    if (de.getRenderingInformation().rendered) {
                        out.add(de)
                    }
                }
            }
        }
        return out
    }

    fun setRoute(tle: Connection, vertices: List<Vertex>?, c: Compaction) {
        val out = tle.getRenderingInformation()
        out.clear()
        var first = true
        var last: Dimension2D? = null
        var prev: Dimension2D? = null
        for (v in vertices!!) {
            val next = addToRoute(out, v, c, last)
            setPortPosition(v, next)
            if (first && last != null && next != null) {
                setTerminatorPositionAndOrientation(tle.getFromDecoration(), last, next)
                first = false
            }
            if (next != null) {
                prev = last
                last = next
            }
        }
        setTerminatorPositionAndOrientation(tle.getToDecoration(), last, prev)
    }

    private fun setPortPosition(v: Vertex, d2: Dimension2D?) {
        if (d2 == null) {
            return
        }
        v.getDiagramElements()
            .filterIsInstance<Port>()
            .forEach {
                it.getRenderingInformation().position = CostedDimension2D(d2.x(), d2.y())
                it.getRenderingInformation().size = CostedDimension2D(0.0, 0.0)
            }
    }

    private fun addToRoute(out: RouteRenderingInformation, v: Vertex, c: Compaction, prev: Dimension2D?): Dimension2D? {
        val horizSeg = c.getHorizontalSegmentSlackOptimisation().getVertexToSlidableMap()[v]
        val vertSeg = c.getVerticalSegmentSlackOptimisation().getVertexToSlidableMap()[v]
        val x2 = vertSeg?.minimumPosition?.toDouble() ?: prev!!.x()
        val y2 = horizSeg?.minimumPosition?.toDouble() ?: prev!!.y()
        val p1: Dimension2D = BasicDimension2D(x2, y2)
        //boolean hop = false;  // should be based on v.
        if (prev != null) {
            if (prev.x() == p1.x() && prev.y() == p1.y()) {
                return null // prevent duplicates in the list
            }
        }
        out.add(p1)
        return p1
    }

    private fun setTerminatorPositionAndOrientation(t: Terminator?, pos: Dimension2D?, from: Dimension2D?) {
        if (t == null) {
            return
        }
        val x1 = pos!!.x() // - getRotatedSize(t,r,Direction.LEFT);
        val y1 = pos.y() // - getRotatedSize(t,r,Direction.UP);
        val x2 = pos.x() // + getRotatedSize(t,r,Direction.RIGHT);
        val y2 = pos.y() // + getRotatedSize(t,r,Direction.DOWN);
        val rri = t.getRenderingInformation()
        rri.position = BasicDimension2D(x1, y1)
        rri.size = BasicDimension2D(x2 - x1, y2 - y1)
    }

    private fun getRotatedSize(t: Terminator?, r: Int, d: Direction): Double {
        var r = r
        var d: Direction? = d
        while (r > 0) {
            d = rotateClockwise(d!!)
            r--
        }
        return t?.getPadding(d!!) ?: 0.0
    }

    protected fun getTerminatorRotation(pos: Dimension2D?, from: Dimension2D?): Int {
        var r = 0
        r = if (from!!.y() == pos!!.y()) {
            // horizontal
            if (from.x() < pos.x()) {
                // right
                2
            } else if (from.x() > pos.x()) {
                // left
                0
            } else {
                throw LogicException()
            }
        } else if (from.x() == pos.x()) {
            // vertical
            if (from.y() < pos.y()) {
                // down
                1
            } else if (from.y() > pos.y()) {
                // up
                3
            } else {
                throw LogicException()
            }
        } else {
            throw LogicException()
        }
        return r
    }
}