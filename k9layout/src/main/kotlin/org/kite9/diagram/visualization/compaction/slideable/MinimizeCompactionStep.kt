package org.kite9.diagram.visualization.compaction.slideable

import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.SizedRectangular
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.segment.Segment
import org.kite9.diagram.visualization.display.CompleteDisplayer
import kotlin.math.max

/**
 * In some sorted order, minimizes the size of Rectangular elements in the
 * diagram.
 *
 * @author robmoffat
 */
class MinimizeCompactionStep(cd: CompleteDisplayer?) : AbstractSizingCompactionStep(cd!!) {
    override fun filter(r: Rectangular, horizontal: Boolean): Boolean {
        return r is SizedRectangular && r.getSizing(horizontal) === DiagramElementSizing.MINIMIZE
    }

    override fun performSizing(r: Rectangular, c: Compaction, horizontal: Boolean) {
        val hsso = c.getHorizontalSegmentSlackOptimisation()
        val hs = hsso.getSlideablesFor(r)
        val vsso = c.getVerticalSegmentSlackOptimisation()
        val vs = vsso.getSlideablesFor(r)
        log.send("Minimizing Distance $r")
        if (horizontal) {
            minimizeDistance(vsso, vs.a!!, vs.b!!)
        } else {
            minimizeDistance(hsso, hs.a!!, hs.b!!)
        }

    }

    private fun minimizeDistance(
        opt: SegmentSlackOptimisation,
        from: Slideable,
        to: Slideable
    ): Int {
        val minDist = from.minimumDistanceTo(to)
        opt.ensureMaximumDistance(from, to, minDist)
        return minDist
    }

    override val prefix: String
        get() = "MINC"

    override val isLoggingEnabled: Boolean
        get() = true

    /**
     * Returns in an order to maximize number of centerings.
     */
    override fun compare(a: Rectangular, b: Rectangular, c: Compaction, horizontal: Boolean): Int {
        if (a.getDepth() != b.getDepth()) {
            return -a.getDepth().compareTo(b.getDepth())
        }
        if (a !is Connected && b !is Connected) {
            return 0
        } else if (a !is Connected) {
            return -1
        } else if (b !is Connected) {
            return 1
        } else {
            // return elements with least number of connections on a side
            val ac = maxLeavings(a, c, horizontal)
            val bc = maxLeavings(b, c, horizontal)
            if (bc != ac) {
                return ac.compareTo(bc)
            }
        }
        return b.getID().compareTo(a.getID())
    }

    private fun maxLeavings(a: Rectangular, c: Compaction, horizontal: Boolean): Int {
        return if (horizontal) {
            maxLeavingsInAxis(a, c.getHorizontalSegmentSlackOptimisation(), c)
        } else {
            maxLeavingsInAxis(a, c.getVerticalSegmentSlackOptimisation(), c)
        }
    }

    private fun maxLeavingsInAxis(a: Rectangular, sso: SegmentSlackOptimisation, c: Compaction): Int {
        val (a1, b) = sso.getSlideablesFor(a)
        return max(leavingsOnSide(a1!!, c), leavingsOnSide(b!!, c))
    }

    private fun leavingsOnSide(a2: Slideable, c: Compaction): Int {
        val connections = getLeavingConnections(a2.underlying, c)
        return connections.size
    }
}