package org.kite9.diagram.visualization.compaction.slideable

import org.kite9.diagram.visualization.compaction.segment.SegmentSlideable
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.SizedRectangular
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.display.CompleteDisplayer

class MaximizeCompactionStep(cd: CompleteDisplayer) : AbstractSizingCompactionStep(cd) {

    override fun filter(r: Rectangular, horiz: Boolean): Boolean {
        return r is SizedRectangular && r.getSizing(horiz) === DiagramElementSizing.MAXIMIZE
    }

    /**
     * Orders top-down
     */
    override fun compare(a: Rectangular, b: Rectangular, c: Compaction, horizontal: Boolean): Int {
        return -a.getDepth().compareTo(b.getDepth())
    }

    override fun performSizing(r: Rectangular, c: Compaction, horizontal: Boolean) {
        log.send("Maximizing Distance $r")
        if (horizontal) {
            val vsso = c.getVerticalSegmentSlackOptimisation()
            val vs = vsso.getSlideablesFor(r)
            if (vs != null) {
                maximizeDistance(vs.a!!, vs.b!!)
            }
        } else {
            val hsso = c.getHorizontalSegmentSlackOptimisation()
            val hs = hsso.getSlideablesFor(r)
            if (hs != null) {
                maximizeDistance(hs.a!!, hs.b!!)
            }
        }
    }

    private fun maximizeDistance(min: SegmentSlideable, max: SegmentSlideable) {
        //val biggestDistance = max.maximumPosition!! - min.minimumPosition!!
        //min.addMinimumForwardConstraint(max, biggestDistance)
        // old way
        max!!.minimumPosition = max.maximumPosition!!
        min!!.maximumPosition = min.minimumPosition
    }

    override val prefix: String?
        get() = "MAXS"
}