package org.kite9.diagram.visualization.compaction

import org.kite9.diagram.visualization.compaction.segment.Segment
import org.kite9.diagram.visualization.orthogonalization.DartFace

/**
 * Embeddings are basically wrappers around outer faces in the diagram.  We compact outer faces from the
 * bottom up.
 *
 * @author robmoffat
 */
interface Embedding {
    /**
     * Used in rectangularization
     */
    val dartFaces: List<DartFace>
    var innerEmbeddings: List<Embedding>
    fun getVerticalSegments(c: Compaction): Collection<Segment>
    fun getHorizontalSegments(c: Compaction): Collection<Segment>
    val isTopEmbedding: Boolean
}