package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.Rectangle
import org.kite9.diagram.model.Diagram
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
class C2CompactionImpl(
    private val diagram: Diagram,
    private val horizontalSegmentSlackOptimisation: C2SlackOptimisation,
    private val verticalSegmentSlackOptimisation: C2SlackOptimisation

) : C2Compaction {

    override fun getVerticalSegments(): List<C2Slideable> {
        return verticalSegmentSlackOptimisation.getAllSlideables()
            .filterIsInstance<C2Slideable>()
    }

    override fun getHorizontalSegments(): List<C2Slideable> {
        return horizontalSegmentSlackOptimisation.getAllSlideables()
            .filterIsInstance<C2Slideable>()
    }


    override fun getHorizontalSegmentSlackOptimisation(): C2SlackOptimisation {
        return horizontalSegmentSlackOptimisation
    }

    override fun getVerticalSegmentSlackOptimisation(): C2SlackOptimisation {
        return verticalSegmentSlackOptimisation
    }

    override fun getSlackOptimisation(horizontal: Boolean): C2SlackOptimisation {
        return if (horizontal) {
            getHorizontalSegmentSlackOptimisation()
        } else {
            getVerticalSegmentSlackOptimisation()
        }
    }

    override fun getDiagram(): Diagram {
        return diagram
    }

}