package org.kite9.diagram.visualization.compaction2.align

import org.kite9.diagram.common.algorithms.so.AlignStyle
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.CompactionStep
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable

/**
 * Figures out how to align connections - usually trying to minimize length.
 *
 * @author robmoffat
 */
class ConnectionAlignmentCompactionStep : CompactionStep {

    override fun compact(c: Compaction, e: Embedding, rc: Compactor) {
        if (e.isTopEmbedding) {
            alignConnections(c.getHorizontalSegments())
            alignConnections(c.getVerticalSegments())
        }
    }

    private fun alignConnections(horizontalSegments: List<ElementSlideable>) {
        horizontalSegments
            .filter { s: ElementSlideable -> s.connections.isNotEmpty() }
            .forEach { s: ElementSlideable -> alignSegment(s) }
    }

    private fun alignSegment(sl: ElementSlideable) {
        if (sl.alignStyle === AlignStyle.MAX) {
            val max = sl.maximumPosition
            sl.minimumPosition = max!!
        } else if (sl.alignStyle === AlignStyle.MIN) {
            val min = sl.minimumPosition
            sl.maximumPosition = min
        } else {
            val balance = sl.adjoiningSegmentBalance
            val pos = if (balance == 0) {
                val slack = sl.maximumPosition!! - sl.minimumPosition
                sl.minimumPosition + slack / 2
            } else if (balance < 0) {
                sl.minimumPosition
            } else {
                sl.maximumPosition!!
            }
            sl.minimumPosition = pos
            sl.maximumPosition = pos
        }
    }
}