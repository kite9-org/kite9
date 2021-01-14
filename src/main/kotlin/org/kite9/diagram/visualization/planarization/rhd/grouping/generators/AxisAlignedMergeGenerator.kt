package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group.linkManager
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.processAllLeavingLinks
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis.Companion.getState
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor.process
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.subsetGroup
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.generators.GeneratorBasedGroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.generators.AbstractAlignedMergeGenerator
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.generators.AxisAlignedMergeGenerator
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import java.util.EnumSet

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

    override fun processPossibleAligningGroups(g1: GroupPhase.Group, d: Direction, lp: LinkProcessor) {
        val mask = DirectedLinkManager.createMask(null, false, false, d)
        g1.linkManager.processAllLeavingLinks(true, mask, lp)
    }

    override fun getAlignmentDirections(g1: GroupPhase.Group): Set<Direction> {
        val state = getState(g1)
        return when (state) {
            MergePlane.UNKNOWN -> ALL
            MergePlane.X_FIRST_MERGE -> LEFT_RIGHT
            MergePlane.Y_FIRST_MERGE -> UP_DOWN
        }
        throw LogicException("Unknown state: $state")
    }

    override fun processAlignedGroupsInAxis(
        alignedGroup: GroupPhase.Group, ms: BasicMergeState, axis: DirectedGroupAxis,
        mp: MergePlane, d: Direction, lp: LinkProcessor
    ) {
        val alignedGroups = getAlignedGroups(alignedGroup, axis, mp, d)
        for (g in alignedGroups) {
            lp.process(alignedGroup, g, null)
        }
    }

    protected fun getAlignedGroups(
        g: GroupPhase.Group,
        axis: DirectedGroupAxis?,
        mp: MergePlane?,
        d: Direction?
    ): Collection<GroupPhase.Group> {
        val mask = DirectedLinkManager.createMask(mp, true, false, d)
        return g.linkManager.subsetGroup(mask)
    }

    companion object {
        val UP_DOWN: Set<Direction> = EnumSet.of(Direction.UP, Direction.DOWN)
        val LEFT_RIGHT: Set<Direction> = EnumSet.of(Direction.LEFT, Direction.RIGHT)
        val ALL: Set<Direction> = EnumSet.allOf(Direction::class.java)
    }
}