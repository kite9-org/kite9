package org.kite9.diagram.visualization.compaction

import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.Rectangle
import org.kite9.diagram.visualization.compaction.segment.SegmentSlackOptimisation
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.DartFace
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization

interface Compaction {

    fun getHorizontalSegmentSlackOptimisation(): SegmentSlackOptimisation
    fun getVerticalSegmentSlackOptimisation(): SegmentSlackOptimisation
    fun getSlackOptimisation(horizontal: Boolean): SegmentSlackOptimisation
    fun getOrthogonalization(): Orthogonalization
    fun getVerticalSegments(): List<ElementSlideable>
    fun getHorizontalSegments(): List<ElementSlideable>
    fun getHorizontalVertexSegmentMap(): Map<Vertex, ElementSlideable>
    fun getVerticalVertexSegmentMap(): Map<Vertex, ElementSlideable>

    /**
     * For an internal face, returns the empty rectangle in the centre of the space that can
     * be used to insert subface contents.
     *
     * Rectangle is in top, right, bottom, left order.
     */
    fun getFaceSpace(df: DartFace): Rectangle<FaceSide>?
    fun createFaceSpace(df: DartFace, r: Rectangle<FaceSide>)
    fun setFaceSpaceToDone(df: DartFace)
    fun getSegmentForDart(d: Dart): ElementSlideable
    fun getTopEmbedding(): Embedding

    companion object {

        val DONE_FACE_SIDE = FaceSide(null, setOf())

		val DONE = Rectangle<FaceSide>(DONE_FACE_SIDE, DONE_FACE_SIDE, DONE_FACE_SIDE, DONE_FACE_SIDE)
    }
}