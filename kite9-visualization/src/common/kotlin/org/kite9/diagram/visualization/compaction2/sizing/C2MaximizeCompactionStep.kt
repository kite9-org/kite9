package org.kite9.diagram.visualization.compaction2.sizing

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.visualization.compaction.segment.SegmentSlideable
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.SizedRectangular
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.SlideableSet
import org.kite9.diagram.visualization.display.CompleteDisplayer

class C2MaximizeCompactionStep(cd: CompleteDisplayer) : AbstractC2SizingCompactionStep(cd) {

    override fun filter(r: Rectangular, d: Dimension): Boolean {
        return r is SizedRectangular && r.getSizing(d.isHoriz()) === DiagramElementSizing.MAXIMIZE
    }

    /**
     * Orders top-down
     */
    override fun compare(a: Rectangular, b: Rectangular, c: C2Compaction, d: Dimension): Int {
        return -a.getDepth().compareTo(b.getDepth())
    }

    override fun performSizing(r: Rectangular, c: C2Compaction, d: Dimension) {
        log.send("Maximizing Distance $r")
        val vsso = c.getSlackOptimisation(d)
        val ss = vsso.getSlideablesFor(r)
        maximizeDistance(ss)
    }

    private fun maximizeDistance(ss: SlideableSet?) {
        if (ss is RectangularSlideableSet) {
            val start = ss.getRectangularsOnSide(Side.START)
            val end = ss.getRectangularsOnSide(Side.END)
            end.forEach { it.minimumPosition = it.maximumPosition!! }
            start.forEach { it.maximumPosition = it.minimumPosition }
        }
    }

    override val prefix: String?
        get() = "MAXS"

}