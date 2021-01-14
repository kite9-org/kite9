package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis.Companion.getState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane

/**
 * Generates nearest-neighbour merge options for perpendicular groups in axis.  These can span containers,
 * and generally have the highest priority.
 *
 * @author robmoffat
 */
class PerpendicularDirectedMergeGenerator(
    gp: GroupPhase, ms: BasicMergeState,
    grouper: GeneratorBasedGroupingStrategy
) : AbstractMergeGenerator(
    gp, ms, grouper
) {

    override fun generate(poll: GroupPhase.Group) {
        val state = getState(poll)
        if (state === MergePlane.X_FIRST_MERGE || state === MergePlane.UNKNOWN) {
            // horizontal merges
            generateMergesInDirection(poll, false, MergePlane.X_FIRST_MERGE)
        }
        if (state === MergePlane.Y_FIRST_MERGE || state === MergePlane.UNKNOWN) {
            // vertical merges
            generateMergesInDirection(poll, true, MergePlane.Y_FIRST_MERGE)
        }
    }

    private fun generateMergesInDirection(
        poll: GroupPhase.Group,
        horizontal: Boolean,
        mp: MergePlane
    ) {
        val dlm = poll.linkManager as DirectedLinkManager
        val mask = if (horizontal) DirectedLinkManager.createMask(
            mp,
            true,
            false,
            Direction.LEFT,
            Direction.RIGHT
        ) else DirectedLinkManager.createMask(mp, true, true, Direction.UP, Direction.DOWN)
        for (g in dlm.subsetGroup(mask)) {
            addMergeOption(poll, g, null, null)
        }
    }

    override fun getMyBestPriority(): Int {
        return AbstractRuleBasedGroupingStrategy.PERP_NEIGHBOUR
    }

    override fun getCode(): String {
        return "PerpendicularNeighbour"
    }
}