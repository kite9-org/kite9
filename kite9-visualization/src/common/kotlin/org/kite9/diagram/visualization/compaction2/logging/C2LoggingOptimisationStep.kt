package org.kite9.diagram.visualization.compaction2.logging

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.compaction.segment.SegmentSlackOptimisation
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

class C2LoggingOptimisationStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override val prefix: String
        get() = "LOPS"
    override val isLoggingEnabled: Boolean
        get() = true

    override fun compact(c: C2Compaction, g:Group) {
        optimise(c, c.getSlackOptimisation(Dimension.H), c.getSlackOptimisation(Dimension.V))
    }

    fun optimise(
        c: C2Compaction,
        horizontalSegments: C2SlackOptimisation,
        verticalSegments: C2SlackOptimisation
    ) {
        log.send(
            if (log.go()) null else """Minimisation Steps: 
  HorizontalSegments: ${horizontalSegments.getSize()} 
  Vertical Segments: ${verticalSegments.getSize()} T: ${horizontalSegments.getSize() + verticalSegments.getSize()}"""
        )
        log.send(
            if (log.go()) null else """Push Steps: 
  Horizontal Segments: ${horizontalSegments.pushCount} 
  Vertical Segments: ${verticalSegments.pushCount}T: ${horizontalSegments.pushCount + verticalSegments.pushCount}"""
        )
        log.send("Horizontal Segments:", horizontalSegments.getAllSlideables())
        log.send("Vertical Segments:", verticalSegments.getAllSlideables())
    }
}