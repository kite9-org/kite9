package org.kite9.diagram.visualization.compaction.segment

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side

/**
 * Augments SlackOptimisation to keep track of segments underlying the slideables.
 * Also has a vertex-to-slideable map.
 *
 * @author robmoffat
 */
class SegmentSlackOptimisation(segments: List<Segment>, val theDiagram: Diagram) : AbstractSlackOptimisation(), Logable {

    private val vertexToSlidableMap: MutableMap<Vertex, SegmentSlideable> = HashMap()
    private val rectangularElementToSegmentSlideableMap: MutableMap<DiagramElement, OPair<SegmentSlideable?>> = HashMap()

    private fun isRectangular(underlying: DiagramElement): Boolean {
        return underlying is Rectangular
    }

    fun updateMaps(s: SegmentSlideable) {
        val seg = s.underlying
        for (v in seg.getVerticesInSegment()) {
            vertexToSlidableMap[v] = s
        }
        for ((underlying, side) in seg.underlyingInfo) {
            if (isRectangular(underlying)) {
                var parts = rectangularElementToSegmentSlideableMap[underlying]
                if (parts == null) {
                    parts = OPair<SegmentSlideable?>(null, null)
                }
                if (side === Side.START) {
                    parts = OPair(s, parts.b)
                } else if (side === Side.END) {
                    parts = OPair(parts.a, s)
                }
                rectangularElementToSegmentSlideableMap[underlying] = parts

            }
        }
    }

    fun getVertexToSlidableMap(): Map<Vertex, SegmentSlideable?> {
        return vertexToSlidableMap
    }

    override fun getIdentifier(underneath: Any?): String? {
        return (underneath as Segment?)!!.identifier
    }

    override fun initialiseSlackOptimisation() {
        val (a) = rectangularElementToSegmentSlideableMap[theDiagram]!!
        a!!.minimumPosition = 0
    }

    fun getSlideablesFor(de: DiagramElement): OPair<SegmentSlideable?> {
        return rectangularElementToSegmentSlideableMap[de]!!
    }

    init {
        for (s in segments) {
            val sli = SegmentSlideable(this, s)
            s.slideable = sli
            log.send(if (log.go()) null else "Created slideable: $sli")
            _allSlideables.add(sli)
            updateMaps(sli)
        }
        pushCount = 0
        initialiseSlackOptimisation()
    }
}