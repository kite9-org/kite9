package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis.Companion.getState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane

/**
 * Generates single-directed-merge options for any groups in axis.  These can span containers,
 * and generally have the highest priority.
 *
 * @author robmoffat
 */
class AxisSingleMergeGenerator(gp: GroupPhase, ms: BasicMergeState, grouper: GeneratorBasedGroupingStrategy) :
    AbstractMergeGenerator(
        gp, ms, grouper
    ) {
    override fun generate(poll: GroupPhase.Group) {
        log.send(if (log.go()) null else "Generating " + getCode() + " options for " + poll)
        val state = getState(poll)
        if (state === MergePlane.X_FIRST_MERGE || state === MergePlane.UNKNOWN) {
            // horizontal merges
            generateMergesInDirection(poll, ms, Direction.RIGHT, MergePlane.X_FIRST_MERGE)
            generateMergesInDirection(poll, ms, Direction.LEFT, MergePlane.X_FIRST_MERGE)
        }
        if (state === MergePlane.Y_FIRST_MERGE || state === MergePlane.UNKNOWN) {
            // vertical merges
            generateMergesInDirection(poll, ms, Direction.DOWN, MergePlane.Y_FIRST_MERGE)
            generateMergesInDirection(poll, ms, Direction.UP, MergePlane.Y_FIRST_MERGE)
        }
    }

    private fun generateMergesInDirection(poll: GroupPhase.Group, ms: BasicMergeState, d: Direction, mp: MergePlane) {
        val dlm = poll.linkManager as DirectedLinkManager
        val right = dlm.getSingleDirectedMergeOption(d, mp, ms, false)
        if (right != null) {
            addMergeOption(poll, right, null, null)
        }
    }

    override fun getMyBestPriority(): Int {
        return AbstractRuleBasedGroupingStrategy.AXIS_SINGLE_NEIGHBOUR
    }

    override fun getCode(): String {
        return "AxisSingle"
    }
}