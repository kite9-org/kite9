package org.kite9.diagram.visualization.compaction.segment

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.rotateClockwise
import org.kite9.diagram.visualization.compaction.PluggableCompactor
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization

/**
 * This looks at the orthogonal representation and works out from the available Darts what
 * Vertices must be on the same Vertical or Horizontal line.
 *
 * @author robmoffat
 */
class SegmentSlideableBuilder : Logable {

    var log = Kite9Log.instance(this)


    fun buildSegmentList(
        o: Orthogonalization,
        direction: Set<Direction>
    ): SegmentSlackOptimisation {
        val sso = SegmentSlackOptimisation(o.getPlanarization().diagram)
        val out = buildSegmentList(o, direction, if (direction === PluggableCompactor.HORIZONTAL) Dimension.H else Dimension.V, sso)

        for (segmentSlideable in out) {
            sso.updateMaps(segmentSlideable)
        }

        sso.initialiseSlackOptimisation()
        return sso
    }

    private fun buildSegmentList(o: Orthogonalization, planeDirection: Set<Direction>, direction: Dimension, so: SegmentSlackOptimisation): List<SegmentSlideable> {
        val transversePlane: MutableSet<Direction> = LinkedHashSet()
        for (d2 in planeDirection) {
            transversePlane.add(rotateClockwise(d2))
        }
        val result: MutableList<SegmentSlideable> = ArrayList()
        val done: MutableSet<Vertex> = UnorderedSet()
        var segNo = 1
        for (d in o.getAllDarts()) {
            if (planeDirection.contains(d.getDrawDirection())) {
                val v = d.getFrom()
                if (!done.contains(v)) {
                    val out = mutableSetOf<Vertex>()
                    extendSegmentFromVertex(v, planeDirection, out, done)
                    val s = SegmentSlideable(
                        so, direction, segNo++, out
                    )
                    result.add(s)
                }
            }
        }
        log.send("Segments", result)
        return result
    }



    private fun extendSegmentFromVertex(
        v: Vertex,
        planeDirection: Set<Direction?>,
        done: MutableSet<Vertex>,
        out: MutableSet<Vertex>
    ) {
        if (done.contains(v)) return
        out.add(v)
        done.add(v)
        for (e in v.getEdges()) {
            if (e is Dart && planeDirection.contains(e.getDrawDirection())) {
                val other = e.otherEnd(v)
                extendSegmentFromVertex(other, planeDirection, out, done)
            }
        }
    }

    override val prefix: String
        get() = "SEGB"

    override val isLoggingEnabled: Boolean
        get() = true
}