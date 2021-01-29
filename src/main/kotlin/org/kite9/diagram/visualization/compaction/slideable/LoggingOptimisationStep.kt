package org.kite9.diagram.visualization.compaction.slideable

import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.display.CompleteDisplayer

class LoggingOptimisationStep(cd: CompleteDisplayer) : AbstractCompactionStep(cd) {

    override val prefix: String
        get() = "LOPS"
    override val isLoggingEnabled: Boolean
        get() = true

    override fun compact(c: Compaction, r: Embedding, rc: Compactor) {
        log.send("Embedding: $r")
        optimise(c, c.getHorizontalSegmentSlackOptimisation(), c.getVerticalSegmentSlackOptimisation())
    }

    fun optimise(
        c: Compaction?,
        horizontalSegments: SegmentSlackOptimisation,
        verticalSegments: SegmentSlackOptimisation
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