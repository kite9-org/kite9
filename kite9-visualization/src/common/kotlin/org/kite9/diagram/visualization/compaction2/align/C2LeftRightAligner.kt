package org.kite9.diagram.visualization.compaction2.align

import org.kite9.diagram.common.algorithms.so.AlignStyle
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.AlignedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.SizedRectangular
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.VerticalAlignment
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.compaction2.RectangularSlideableSet

/**
 * If you have contradictory alignments, (e.g. thing on left wants to align right, thing on right wants to align left)
 * then this is going to be inconsistent.
 */
class C2LeftRightAligner : Aligner {

    override fun alignFor(co: Container, des: Set<Rectangular>, c: C2Compaction, d: Dimension) {
        val sso = c.getSlackOptimisation(d)
        for (r in des) {
            alignRectangular(r, sso, d)
        }
    }

    private fun alignRectangular(de: Rectangular, sso: C2SlackOptimisation, d: Dimension) {
        val oss = sso.getSlideablesFor(de) as RectangularSlideableSet?
        if (oss != null) {
            alignSegment(oss.l, d)
            alignSegment(oss.r, d)
        }
    }

    private fun getAlignStyle(sl: C2Slideable, d: Dimension) : AlignStyle? {
        return sl.anchors
            .asSequence()
            .map { it.e }
            .filterIsInstance<SizedRectangular>()
            .map { it.getSizing(d.isHoriz())}
            .map { des -> when (des) {
                DiagramElementSizing.MAXIMIZE -> AlignStyle.MAX
                DiagramElementSizing.MINIMIZE -> AlignStyle.MIN
                else -> AlignStyle.MIN
            } }
            .firstOrNull()
    }

    private fun alignSegment(sl: C2Slideable, d: Dimension) {
        val style = getAlignStyle(sl, d)
        if (style == AlignStyle.MAX) {
            val max = sl.maximumPosition
            sl.minimumPosition = max!!
        } else if (style === AlignStyle.MIN) {
            val min = sl.minimumPosition
            sl.maximumPosition = min
        }
    }

    override fun willAlign(de: Rectangular, d: Dimension): Boolean {
        if (de !is AlignedRectangular) {
            return false
        }
        return if (d.isHoriz()) {
            de.getHorizontalAlignment() === HorizontalAlignment.LEFT ||
                    de.getHorizontalAlignment() === HorizontalAlignment.RIGHT
        } else {
            de.getVerticalAlignment() === VerticalAlignment.TOP ||
                    de.getVerticalAlignment() === VerticalAlignment.BOTTOM
        }
    }
}