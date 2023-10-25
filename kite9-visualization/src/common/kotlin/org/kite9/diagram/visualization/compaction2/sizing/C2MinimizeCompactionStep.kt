package org.kite9.diagram.visualization.compaction2.sizing

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.visualization.compaction.segment.SegmentSlideable
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.SizedRectangular
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction.segment.SegmentSlackOptimisation
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.display.CompleteDisplayer
import kotlin.math.max

/**
 * In some sorted order, minimizes the size of Rectangular elements in the
 * diagram.
 *
 * @author robmoffat
 */
class C2MinimizeCompactionStep(cd: CompleteDisplayer?) : AbstractC2SizingCompactionStep(cd!!) {
    override fun filter(r: Rectangular, d: Dimension): Boolean {
        return r is SizedRectangular && r.getSizing(d.isHoriz()) === DiagramElementSizing.MINIMIZE
    }

    override fun performSizing(r: Rectangular, c: C2Compaction, d: Dimension) {
        val hsso = c.getSlackOptimisation(d)
        val hs = hsso.getSlideablesFor(r)
        if (hs is RectangularSlideableSet) {
           log.send("Minimizing Distance $r")
            minimizeDistance(hsso, hs)
        }

    }

    private fun minimizeDistance(
        opt: C2SlackOptimisation,
        set: RectangularSlideableSet,
    ) {
        val left = set.getRectangularOnSide(Side.START)
        val right = set.getRectangularOnSide(Side.END)
        val minDist = left.minimumDistanceTo(right)
        opt.ensureMaximumDistance(left,right, minDist)
    }

    override val prefix: String
        get() = "MINC"

    override val isLoggingEnabled: Boolean
        get() = true

    /**
     * Returns in an order to maximize number of centerings.
     */
    override fun compare(a: Rectangular, b: Rectangular, c: C2Compaction, d: Dimension): Int {
        if (a.getDepth() != b.getDepth()) {
            return -a.getDepth().compareTo(b.getDepth())
        }
        if (a !is ConnectedRectangular && b !is ConnectedRectangular) {
            return 0
        } else if (a !is ConnectedRectangular) {
            return -1
        } else if (b !is ConnectedRectangular) {
            return 1
        } else {
            // return elements with least number of connections on a side
            val ac = maxLeavings(a, c, d)
            val bc = maxLeavings(b, c, d)
            if (bc != ac) {
                return ac.compareTo(bc)
            }
        }
        return b.getID().compareTo(a.getID())
    }

    private fun maxLeavings(a: Rectangular, c: C2Compaction, d: Dimension): Int {
        return maxLeavingsInAxis(a, c.getSlackOptimisation(d), c)
    }

    private fun maxLeavingsInAxis(a: Rectangular, sso: C2SlackOptimisation, c: C2Compaction): Int {
        val ss = sso.getSlideablesFor(a)
        return max(leavingsOnSide(ss!!, c, Side.START), leavingsOnSide(ss!!, c, Side.END))
    }

    private fun leavingsOnSide(ss: RectangularSlideableSet, c: C2Compaction, side: Side): Int {
        val connections = getLeavingConnections(ss, c)
        return connections.size
    }
    fun getLeavingConnections(a2: RectangularSlideableSet, c: C2Compaction): Collection<Connection> {
        // TODO: Implement this
        return emptyList()
    }
}