package org.kite9.diagram.visualization.compaction.slideable

import org.kite9.diagram.visualization.compaction.Embedding.isTopEmbedding
import org.kite9.diagram.visualization.compaction.Compaction.getHorizontalSegmentSlackOptimisation
import org.kite9.diagram.visualization.compaction.Compaction.getVerticalSegmentSlackOptimisation
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation.getSlideablesFor
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation.theDiagram
import org.kite9.diagram.common.objects.OPair.b
import org.kite9.diagram.common.algorithms.so.Slideable.minimumPosition
import org.kite9.diagram.common.algorithms.so.Slideable.maximumPosition
import org.kite9.diagram.logging.Kite9Log.send
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.common.algorithms.so.Slideable

/**
 * Ensures that the overall diagram width is minimal.
 * @author robmoffat
 */
class DiagramSizeCompactionStep(cd: CompleteDisplayer?) : AbstractCompactionStep(cd!!) {
    override var log = Kite9Log(this)
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