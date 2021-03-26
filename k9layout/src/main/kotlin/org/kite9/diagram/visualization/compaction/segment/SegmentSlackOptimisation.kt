package org.kite9.diagram.visualization.compaction.segment

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable

/**
 * Augments SlackOptimisation to keep track of segments underlying the slideables.
 * Also has a vertex-to-slideable map.
 *
 * @author robmoffat
 */
class SegmentSlackOptimisation(val theDiagram: Diagram) : AbstractSlackOptimisation(), Logable {

    private val vertexToSlidableMap: MutableMap<Vertex, SegmentSlideable> = HashMap()
    private val rectangularElementToSegmentSlideableMap: MutableMap<DiagramElement, OPair<SegmentSlideable?>> = HashMap()

    private fun isRectangular(underlying: DiagramElement): Boolean {
        return underlying is Rectangular
    }

    override fun updateMaps(s: Slideable) {
        log.send(if (log.go()) null else "Added slideable: $s")
        _allSlideables.add(s)
        if (s is SegmentSlideable) {
            for (v in s.verticesOnSlideable) {
                vertexToSlidableMap[v] = s
            }
            for ((underlying, side) in s.underlyingInfo) {
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
    }

    fun getVertexToSlidableMap(): Map<Vertex, SegmentSlideable?> {
        return vertexToSlidableMap
    }

    override fun initialiseSlackOptimisation() {
        val (a) = rectangularElementToSegmentSlideableMap[theDiagram]!!
        a!!.minimumPosition = 0
    }

    fun getSlideablesFor(de: DiagramElement): OPair<SegmentSlideable?> {
        return rectangularElementToSegmentSlideableMap[de]!!
    }

}