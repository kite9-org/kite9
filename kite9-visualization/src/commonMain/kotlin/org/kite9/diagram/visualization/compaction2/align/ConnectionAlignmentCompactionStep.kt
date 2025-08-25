package org.kite9.diagram.visualization.compaction2.align

import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2CompactionStep
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * Figures out how to align connections - usually trying to minimize length.
 *
 * @author robmoffat
 */
class ConnectionAlignmentCompactionStep : C2CompactionStep {

    //    override fun compact(c: C2Compaction, e: C2Embedding, rc: Compactor) {
//        if (e.isTopEmbedding) {
//            alignConnections(c.getHorizontalSegments())
//            alignConnections(c.getVerticalSegments())
//        }
//    }
//
//    private fun alignConnections(horizontalSegments: List<ElementSlideable>) {
//        horizontalSegments
//            .filter { s: ElementSlideable -> s.connections.isNotEmpty() }
//            .forEach { s: ElementSlideable -> alignSegment(s) }
//    }
//
//    private fun alignSegment(sl: ElementSlideable) {
//        if (sl.alignStyle === AlignStyle.MAX) {
//            val max = sl.maximumPosition
//            sl.minimumPosition = max!!
//        } else if (sl.alignStyle === AlignStyle.MIN) {
//            val min = sl.minimumPosition
//            sl.maximumPosition = min
//        } else {
//            val balance = sl.adjoiningSegmentBalance
//            val pos = if (balance == 0) {
//                val slack = sl.maximumPosition!! - sl.minimumPosition
//                sl.minimumPosition + slack / 2
//            } else if (balance < 0) {
//                sl.minimumPosition
//            } else {
//                sl.maximumPosition!!
//            }
//            sl.minimumPosition = pos
//            sl.maximumPosition = pos
//        }
//    }
    override fun compact(c: C2Compaction, g: Group) {
        TODO("Not yet implemented")
    }
}