package org.kite9.diagram.visualization.compaction.align

import org.kite9.diagram.common.algorithms.so.AlignStyle
import org.kite9.diagram.model.AlignedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.VerticalAlignment
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.segment.Segment
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation

/**
 * If you have contradictory alignments, (e.g. thing on left wants to align right, thing on right wants to align left)
 * then this is going to be inconsistent.
 */
class LeftRightAligner : Aligner {

    override fun alignFor(co: Container, des: Set<Rectangular>, c: Compaction, horizontal: Boolean) {
        val sso = if (horizontal) c.getVerticalSegmentSlackOptimisation() else c.getHorizontalSegmentSlackOptimisation()
        for (r in des) {
            alignRectangular(r, sso)
        }
    }

    private fun alignRectangular(de: Rectangular, sso: SegmentSlackOptimisation) {
        val oss = sso.getSlideablesFor(de)
        if (oss != null) {
            alignSegment(oss.a!!.underlying)
            alignSegment(oss.b!!.underlying)
        }
    }

    private fun alignSegment(s: Segment) {
        val sl = s.slideable
        if (s.alignStyle === AlignStyle.MAX) {
            val max = sl!!.maximumPosition
            sl.minimumPosition = max!!
        } else if (s.alignStyle === AlignStyle.MIN) {
            val min = sl!!.minimumPosition
            sl.maximumPosition = min
        }
    }

    override fun willAlign(de: Rectangular, horizontal: Boolean): Boolean {
        if (de !is AlignedRectangular) {
            return false
        }
        return if (horizontal) {
            de.getHorizontalAlignment() === HorizontalAlignment.LEFT ||
                    de.getHorizontalAlignment() === HorizontalAlignment.RIGHT
        } else {
            de.getVerticalAlignment() === VerticalAlignment.TOP ||
                    de.getVerticalAlignment() === VerticalAlignment.BOTTOM
        }
    }
}