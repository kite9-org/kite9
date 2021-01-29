package org.kite9.diagram.visualization.compaction

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.segment.Segment
import org.kite9.diagram.visualization.compaction.segment.SegmentBuilder
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.DartFace
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization

/**
 * Superclass for all segment-based compaction methods.   The process works by converting each face into
 * a set of segments, where segments represent horizontal or vertical positions.
 *
 *
 * @author robmoffat
 */
class PluggableCompactor(protected var steps: Array<CompactionStep>) : Compactor {

    protected var sb = SegmentBuilder()

    override fun compactDiagram(o: Orthogonalization): Compaction {
        val horizontal = buildSegmentList(o, HORIZONTAL)
        val vertical = buildSegmentList(o, VERTICAL)
        val dartToSegmentMap = calculateDartToSegmentMap(horizontal, vertical)
        val horizontalSegmentMap = createVertexSegmentMap(horizontal)
        val verticalSegmentMap = createVertexSegmentMap(vertical)
        val topEmbedding = generateEmbeddings(o)
        val compaction = instantiateCompaction(
            o,
            horizontal,
            vertical,
            dartToSegmentMap,
            horizontalSegmentMap,
            verticalSegmentMap,
            topEmbedding
        )
        compact(compaction.getTopEmbedding(), compaction)
        return compaction
    }

    private fun generateEmbeddings(o: Orthogonalization): Embedding {
        val done: MutableMap<DartFace, EmbeddingImpl> = LinkedHashMap()
        var embeddingNumber = 0
        for (dartFace in o.getFaces()) {
            if (!done.containsKey(dartFace)) {
                var touching: MutableSet<DartFace> = HashSet()
                touching = getTouchingFaces(touching, dartFace, o)
                val ei = EmbeddingImpl(embeddingNumber++, ArrayList(touching))
                touching.forEach { df: DartFace -> done[df] = ei }
            }
        }
        for (e in LinkedHashSet(done.values)) {
            val contained =
                e.dartFaces
                    .flatMap { it.containedFaces }
            val inside: List<Embedding> = contained
                .map { df: DartFace -> done[df]!! }
            e.innerEmbeddings = inside
        }

        // return just the top one
        val topEmbedding = done.values.stream().filter { e: EmbeddingImpl -> isTopEmbedding(e) }
            .findFirst().orElseThrow { LogicException("No top embedding") }
        topEmbedding.isTopEmbedding = true
        return topEmbedding
    }

    private fun isTopEmbedding(e: Embedding): Boolean {
        return e.dartFaces.stream().anyMatch { df: DartFace -> df.outerFace && df.getContainedBy() == null }
    }

    private fun getTouchingFaces(
        touching: MutableSet<DartFace>,
        dartFace: DartFace,
        o: Orthogonalization
    ): MutableSet<DartFace> {
        var touching = touching
        val todo = touching.add(dartFace)
        if (todo) {
            for ((dart) in dartFace.dartsInFace) {
                val faces = o.getDartFacesForDart(dart)
                for (f2 in faces!!) {
                    if (f2 !== dartFace) {
                        touching = getTouchingFaces(touching, f2, o)
                    }
                }
            }
        }
        return touching
    }

    protected fun instantiateCompaction(
        o: Orthogonalization,
        horizontal: List<Segment>,
        vertical: List<Segment>,
        dartToSegmentMap: Map<Dart, Segment>,
        horizontalSegmentMap: Map<Vertex, Segment>,
        verticalSegmentMap: Map<Vertex, Segment>,
        topEmbedding: Embedding
    ): Compaction {
        return CompactionImpl(
            o,
            horizontal,
            vertical,
            horizontalSegmentMap,
            verticalSegmentMap,
            dartToSegmentMap,
            topEmbedding
        )
    }

    private fun calculateDartToSegmentMap(h1: List<Segment>, v1: List<Segment>): Map<Dart, Segment> {
        val out: MutableMap<Dart, Segment> = HashMap()
        h1.stream().forEach { s: Segment -> addSegmentsToMap(out, s) }
        v1.stream().forEach { s: Segment -> addSegmentsToMap(out, s) }
        return out
    }

    private fun addSegmentsToMap(dartSegmentMap: MutableMap<Dart, Segment>, segment: Segment) {
        for (d in segment.dartsInSegment) {
            dartSegmentMap[d] = segment
        }
    }

    override fun compact(e: Embedding, c: Compaction) {
        for (step in steps) {
            step.compact(c, e, this)
        }
    }

    protected fun createVertexSegmentMap(segs: List<Segment>): Map<Vertex, Segment> {
        val vertexToSegmentMap: MutableMap<Vertex, Segment> = HashMap()
        for (segment in segs) {
            for (v in segment.getVerticesInSegment()) {
                vertexToSegmentMap[v] = segment
            }
        }
        return vertexToSegmentMap
    }

    fun buildSegmentList(
        o: Orthogonalization,
        direction: Set<Direction>
    ): List<Segment> {
        return sb.buildSegmentList(o, direction, if (direction === HORIZONTAL) Dimension.H else Dimension.V)
    }

    companion object {
        val VERTICAL = setOf<Direction>(Direction.UP, Direction.DOWN)
        val HORIZONTAL = setOf<Direction>(Direction.LEFT, Direction.RIGHT)
    }
}