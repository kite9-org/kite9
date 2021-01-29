package org.kite9.diagram.visualization.compaction.align

import org.kite9.diagram.common.algorithms.so.AlignStyle
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.CompactionStep
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.Embedding
import org.kite9.diagram.visualization.compaction.segment.Segment

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

    private fun alignConnections(horizontalSegments: List<Segment>) {
        horizontalSegments.stream()
            .filter { s: Segment -> s.connections.size > 0 }
            .forEach { s: Segment -> alignSegment(s) }
    }

    private fun alignSegment(s: Segment) {
        val sl = s.slideable
        if (s.alignStyle === AlignStyle.MAX) {
            val max = sl!!.maximumPosition
            sl.minimumPosition = max!!
        } else if (s.alignStyle === AlignStyle.MIN) {
            val min = sl!!.minimumPosition
            sl.maximumPosition = min
        } else {
            val balance = s.adjoiningSegmentBalance
            var pos = 0
            val slideable = s.slideable!!
            pos = if (balance == 0) {
                val slack = slideable.maximumPosition!! - slideable.minimumPosition
                slideable.minimumPosition + slack / 2
            } else if (balance < 0) {
                slideable.minimumPosition
            } else {
                slideable.maximumPosition!!
            }
            slideable.minimumPosition = pos
            slideable.maximumPosition = pos
        }
    }
}