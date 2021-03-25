package org.kite9.diagram.visualization.compaction

import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.Rectangle
import org.kite9.diagram.visualization.compaction.segment.Segment
import org.kite9.diagram.visualization.compaction.segment.SegmentSlackOptimisation
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.DartFace
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization

/**
 * Flyweight class that handles the state of the compaction as it goes along.
 * Contains lots of utility methods too.
 *
 *
 * @author robmoffat
 */
class CompactionImpl(
    private val orthogonalization: Orthogonalization,
    private val horizontalSegments: List<Segment>,
    private val verticalSegments: List<Segment>,
    private val hMap: Map<Vertex, Segment>,
    private val vMap: Map<Vertex, Segment>,
    private val dartToSegmentMap: Map<Dart, Segment>,
    topEmbedding: Embedding
) : Compaction {
    override fun getOrthogonalization(): Orthogonalization {
        return orthogonalization
    }

    override fun getVerticalSegments(): List<Segment> {
        return verticalSegments
    }

    override fun getHorizontalSegments(): List<Segment> {
        return horizontalSegments
    }

    private val topEmbedding: Embedding
    private val horizontalSegmentSlackOptimisation: SegmentSlackOptimisation
    private val verticalSegmentSlackOptimisation: SegmentSlackOptimisation
    override fun getHorizontalSegmentSlackOptimisation(): SegmentSlackOptimisation {
        return horizontalSegmentSlackOptimisation
    }

    override fun getVerticalSegmentSlackOptimisation(): SegmentSlackOptimisation {
        return verticalSegmentSlackOptimisation
    }

    override fun getHorizontalVertexSegmentMap(): Map<Vertex, Segment> {
        return hMap
    }

    override fun getFaceSpace(df: DartFace): Rectangle<FaceSide>? {
        return faceSpaces[df]
    }

    override fun getVerticalVertexSegmentMap(): Map<Vertex, Segment> {
        return vMap
    }

    private val faceSpaces: MutableMap<DartFace, Rectangle<FaceSide>> = HashMap()

    override fun createFaceSpace(df: DartFace, border: Rectangle<FaceSide>) {
        faceSpaces[df] = border
    }

    override fun setFaceSpaceToDone(df: DartFace) {
        faceSpaces[df] = Compaction.DONE
    }



    override fun getSegmentForDart(r: Dart): Segment {
        val out = dartToSegmentMap[r]
        return out!!
    }

    override fun getSlackOptimisation(horizontal: Boolean): SegmentSlackOptimisation {
        return if (horizontal) {
            getHorizontalSegmentSlackOptimisation()
        } else {
            getVerticalSegmentSlackOptimisation()
        }
    }

    override fun getTopEmbedding(): Embedding {
        return topEmbedding
    }

    init {
        val diagram = orthogonalization.getPlanarization().diagram
        horizontalSegmentSlackOptimisation = SegmentSlackOptimisation(horizontalSegments, diagram)
        verticalSegmentSlackOptimisation = SegmentSlackOptimisation(verticalSegments, diagram)
        this.topEmbedding = topEmbedding
    }
}