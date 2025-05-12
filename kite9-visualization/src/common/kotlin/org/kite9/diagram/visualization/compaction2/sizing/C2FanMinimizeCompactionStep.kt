package org.kite9.diagram.visualization.compaction2.sizing

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Connected
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * In some sorted order, minimizes the size of Rectangular elements in the
 * diagram.
 *
 * @author robmoffat
 */
class C2FanMinimizeCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    private fun minimizeDistance(
        opt: C2SlackOptimisation,
        left: C2Slideable,
        right: C2Slideable
    ) {
        val minDist = left.minimumDistanceTo(right)
        opt.ensureMaximumDistance(left,right, minDist)
    }

    override fun compact(c: C2Compaction, g: Group) {
        val soh = c.getSlackOptimisation(Dimension.H)
        val sov = c.getSlackOptimisation(Dimension.V)

        minimizeFans(soh)
        minimizeFans(sov)

    }

    private fun minimizeFans(so: C2SlackOptimisation) {
       so.getLaneGroups().forEach { v ->
            val sortedSlideables = v.sortedBy { it.minimumPosition }
            minimizeDistance(so, sortedSlideables.first(), sortedSlideables.last())
        }
    }

    override val prefix: String
        get() = "FANM"

    override val isLoggingEnabled: Boolean
        get() = true


}