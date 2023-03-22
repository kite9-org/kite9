package org.kite9.diagram.visualization.compaction

import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.segment.SegmentSlackOptimisation
import org.kite9.diagram.visualization.compaction.segment.SegmentSlideable
import org.kite9.diagram.visualization.compaction.segment.SegmentSlideableBuilder
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
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

    protected var sb = SegmentSlideableBuilder()

    override fun compactDiagram(o: Orthogonalization): Compaction {
        val horizontal = sb.buildSegmentList(o, HORIZONTAL)
        val vertical = sb.buildSegmentList(o, VERTICAL)
        val dartToSegmentMap = calculateDartToSegmentMap(horizontal.getAllSlideables(), vertical.getAllSlideables())
        val horizontalSegmentMap = createVertexSegmentMap(horizontal.getAllSlideables())
        val verticalSegmentMap = createVertexSegmentMap(vertical.getAllSlideables())
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
        val topEmbedding = done.values.first { isTopEmbedding(it) }
        topEmbedding.isTopEmbedding = true
        return topEmbedding
    }

    private fun isTopEmbedding(e: Embedding): Boolean {
        return e.dartFaces.firstOrNull { df: DartFace -> df.outerFace && df.getContainedBy() == null } != null
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
        horizontal: SegmentSlackOptimisation,
        vertical: SegmentSlackOptimisation,
        dartToSegmentMap: Map<Dart, ElementSlideable>,
        horizontalSegmentMap: Map<Vertex, ElementSlideable>,
        verticalSegmentMap: Map<Vertex, ElementSlideable>,
        topEmbedding: Embedding
    ): Compaction {
        return CompactionImpl(
            o,
            horizontalSegmentMap,
            verticalSegmentMap,
            dartToSegmentMap,
            topEmbedding,
            horizontal,
            vertical
        )
    }

    private fun calculateDartToSegmentMap(h1: Collection<Slideable>, v1: Collection<Slideable>): Map<Dart, ElementSlideable> {
        val out: MutableMap<Dart, ElementSlideable> = HashMap()
        h1.forEach { addSegmentsToMap(out, it) }
        v1.forEach { addSegmentsToMap(out, it) }
        return out
    }

    private fun addSegmentsToMap(dartSegmentMap: MutableMap<Dart, ElementSlideable>, segment: Slideable) {
        if (segment is SegmentSlideable) {
            for (d in segment.dartsInSegment) {
                dartSegmentMap[d] = segment
            }
        }
    }

    override fun compact(e: Embedding, c: Compaction) {
        for (step in steps) {
            step.compact(c, e, this)
        }
    }

    protected fun createVertexSegmentMap(segs: Collection<Slideable>): Map<Vertex, ElementSlideable> {
        val vertexToSegmentMap: MutableMap<Vertex, ElementSlideable> = HashMap()
        for (segment in segs) {
            if (segment is ElementSlideable) {
                for (v in segment.verticesOnSlideable) {
                    vertexToSegmentMap[v] = segment
                }
            }
        }
        return vertexToSegmentMap
    }


    companion object {
        val VERTICAL = setOf<Direction>(Direction.UP, Direction.DOWN)
        val HORIZONTAL = setOf<Direction>(Direction.LEFT, Direction.RIGHT)
    }
}