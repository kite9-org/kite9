package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.compaction.segment.SegmentSlackOptimisation
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization

interface C2Compaction {


    fun getHorizontalSegmentSlackOptimisation(): C2SlackOptimisation
    fun getVerticalSegmentSlackOptimisation(): C2SlackOptimisation
    fun getSlackOptimisation(horizontal: Boolean): C2SlackOptimisation
    fun getDiagram(): Diagram
    fun getVerticalSegments(): List<C2Slideable>
    fun getHorizontalSegments(): List<C2Slideable>
}