package org.kite9.diagram.visualization.compaction

import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.Rectangle
import org.kite9.diagram.visualization.compaction.segment.SegmentSlackOptimisation
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
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
    private val hMap: Map<Vertex, ElementSlideable>,
    private val vMap: Map<Vertex, ElementSlideable>,
    private val dartToSegmentMap: Map<Dart, ElementSlideable>,
    private val topEmbedding: Embedding,
    private val horizontalSegmentSlackOptimisation: SegmentSlackOptimisation,
    private val verticalSegmentSlackOptimisation: SegmentSlackOptimisation
) : Compaction {

    override fun getOrthogonalization(): Orthogonalization {
        return orthogonalization
    }

    override fun getVerticalSegments(): List<ElementSlideable> {
        return verticalSegmentSlackOptimisation.getAllSlideables()
            .filterIsInstance<ElementSlideable>()
    }

    override fun getHorizontalSegments(): List<ElementSlideable> {
        return horizontalSegmentSlackOptimisation.getAllSlideables()
            .filterIsInstance<ElementSlideable>()
    }


    override fun getHorizontalSegmentSlackOptimisation(): SegmentSlackOptimisation {
        return horizontalSegmentSlackOptimisation
    }

    override fun getVerticalSegmentSlackOptimisation(): SegmentSlackOptimisation {
        return verticalSegmentSlackOptimisation
    }

    override fun getHorizontalVertexSegmentMap(): Map<Vertex, ElementSlideable> {
        return hMap
    }

    override fun getFaceSpace(df: DartFace): Rectangle<FaceSide>? {
        return faceSpaces[df]
    }

    override fun getVerticalVertexSegmentMap(): Map<Vertex, ElementSlideable> {
        return vMap
    }

    private val faceSpaces: MutableMap<DartFace, Rectangle<FaceSide>> = HashMap()

    override fun createFaceSpace(df: DartFace, border: Rectangle<FaceSide>) {
        faceSpaces[df] = border
    }

    override fun setFaceSpaceToDone(df: DartFace) {
        faceSpaces[df] = Compaction.DONE
    }



    override fun getSegmentForDart(r: Dart): ElementSlideable {
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
}