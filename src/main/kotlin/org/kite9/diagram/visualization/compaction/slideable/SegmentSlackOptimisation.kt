package org.kite9.diagram.visualization.compaction.slideable

import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.segment.Segment
import org.kite9.diagram.visualization.compaction.segment.Side

/**
 * Augments SlackOptimisation to keep track of segments underlying the slideables.
 * Also has a vertex-to-slideable map.
 *
 * @author robmoffat
 */
class SegmentSlackOptimisation(segments: List<Segment>, val theDiagram: Diagram) : AbstractSlackOptimisation<Segment>(), Logable {

    private val vertexToSlidableMap: MutableMap<Vertex, Slideable<Segment>> = HashMap()
    private val rectangularElementToSlideableMap: MutableMap<DiagramElement, OPair<Slideable<Segment>?>> = HashMap()

    private fun isRectangular(underlying: DiagramElement): Boolean {
        return underlying is Rectangular
    }

//    protected override fun addedSlideable(s: Slideable<Segment>) {
//        // look for dependencies in the direction given
//        // setupMinimumDistancesDueToDarts(s);
//        updateMaps(s)
//    }

//    override fun addSlideables(s: Collection<Slideable<Segment>>) {
//        for (slideable in s) {
//            updateMaps(slideable)
//        }
//        super.addSlideables(s)
//    }

    fun updateMaps(s: Slideable<Segment>) {
        val seg = s.underlying
        for (v in seg.getVerticesInSegment()) {
            vertexToSlidableMap[v] = s
        }
        for ((underlying, side) in seg.underlyingInfo) {
            if (isRectangular(underlying)) {
                var parts = rectangularElementToSlideableMap[underlying]
                if (parts == null) {
                    parts = OPair<Slideable<Segment>?>(null, null)
                }
                if (side === Side.START) {
                    parts = OPair(s, parts.b)
                } else if (side === Side.END) {
                    parts = OPair(parts.a, s)
                }
                rectangularElementToSlideableMap[underlying] = parts

            }
        }
    }

    fun getVertexToSlidableMap(): Map<Vertex, Slideable<Segment>?> {
        return vertexToSlidableMap
    }

    override fun getIdentifier(underneath: Any?): String? {
        return (underneath as Segment?)!!.identifier
    }

    override fun initialiseSlackOptimisation() {
        val (a) = rectangularElementToSlideableMap[theDiagram]!!
        a!!.minimumPosition = 0
    }

    fun getSlideablesFor(de: DiagramElement): OPair<Slideable<Segment>?> {
        return rectangularElementToSlideableMap[de]!!
    }

    init {
        for (s in segments) {
            val sli = Slideable<Segment>(
                this, s
            )
            s.slideable = sli
            log.send(if (log.go()) null else "Created slideable: $sli")
            _allSlideables.add(sli)
            updateMaps(sli)
        }
        pushCount = 0
        initialiseSlackOptimisation()
    }
}