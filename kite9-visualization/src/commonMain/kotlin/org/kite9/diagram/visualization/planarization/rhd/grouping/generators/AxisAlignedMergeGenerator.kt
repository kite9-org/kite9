package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis.Companion.getState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor

/**
 * Generates in-axis aligned merges.
 *
 * @author robmoffat
 */
class AxisAlignedMergeGenerator(gp: GroupPhase, ms: BasicMergeState, grouper: GeneratorBasedGroupingStrategy) :
    AbstractAlignedMergeGenerator(gp, ms, grouper, false) {

    override fun getMyBestPriority(): Int {
        return AbstractRuleBasedGroupingStrategy.AXIS_ALIGNED
    }

    override fun getCode(): String {
        return "InAxisBuddy"
    }

    override fun processPossibleAligningGroups(g1: Group, d: Direction, lp: LinkProcessor) {
        val mask = DirectedLinkManager.createMask(null, false, false, d)
        g1.linkManager.processAllLeavingLinks(true, mask, lp)
    }

    override fun getAlignmentDirections(g1: Group): Set<Direction> {
        val state = getState(g1)
        return when (state) {
            MergePlane.UNKNOWN -> ALL
            MergePlane.X_FIRST_MERGE -> LEFT_RIGHT
            MergePlane.Y_FIRST_MERGE -> UP_DOWN
        }
        throw LogicException("Unknown state: $state")
    }

    override fun processAlignedGroupsInAxis(
        alignedGroup: Group, ms: BasicMergeState, axis: DirectedGroupAxis,
        mp: MergePlane, d: Direction?, lp: LinkProcessor
    ) {
        val alignedGroups = getAlignedGroups(alignedGroup, axis, mp, d)
        for (g in alignedGroups) {
            lp.process(alignedGroup, g, DirectedLinkManager.NULL)
        }
    }

    protected fun getAlignedGroups(
        g: Group,
        axis: DirectedGroupAxis?,
        mp: MergePlane?,
        d: Direction?
    ): Collection<Group> {
        val mask = DirectedLinkManager.createMask(mp, true, false, d)
        return g.linkManager.subsetGroup(mask)
    }

    companion object {
        val UP_DOWN: Set<Direction> = setOf(Direction.UP, Direction.DOWN)
        val LEFT_RIGHT: Set<Direction> = setOf(Direction.LEFT, Direction.RIGHT)
        val ALL: Set<Direction> = setOf(Direction.LEFT, Direction.DOWN, Direction.RIGHT, Direction.UP)
    }
}