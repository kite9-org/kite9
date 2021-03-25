package org.kite9.diagram.visualization.compaction.slideable

import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.compaction.segment.SegmentSlackOptimisation
import org.kite9.diagram.visualization.display.CompleteDisplayer

/**
 * Ensures that the overall diagram width is minimal.
 * @author robmoffat
 */
class DiagramSizeCompactionStep(cd: CompleteDisplayer?) : AbstractCompactionStep(cd!!) {

    override fun compact(c: Compaction, r: Embedding, rc: Compactor) {
        if (r.isTopEmbedding) {
            setFor(c.getHorizontalSegmentSlackOptimisation())
            setFor(c.getVerticalSegmentSlackOptimisation())
        }
    }

    private fun setFor(o: SegmentSlackOptimisation) {
        val (_, highSide) = o.getSlideablesFor(o.theDiagram)
        val min = highSide!!.minimumPosition
        highSide.maximumPosition = min
        log.send("Set Overall Diagram Size: $highSide")
    }

    override val prefix: String
        get() = "DSCS"

    override val isLoggingEnabled: Boolean
        get() = true
}