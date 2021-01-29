package org.kite9.diagram.visualization.compaction.slideable

import org.kite9.diagram.logging.Kite9Log.send
import org.kite9.diagram.visualization.compaction.Compaction.getHorizontalSegmentSlackOptimisation
import org.kite9.diagram.visualization.compaction.Compaction.getVerticalSegmentSlackOptimisation
import org.kite9.diagram.logging.Kite9Log.go
import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation.getSize
import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation.pushCount
import org.kite9.diagram.common.algorithms.so.AbstractSlackOptimisation.getAllSlideables
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation

class LoggingOptimisationStep(cd: CompleteDisplayer?) : AbstractCompactionStep(cd!!) {
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