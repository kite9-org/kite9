package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis.Companion.getState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor
import java.util.*

/**
 * Looks for perpendicular aligned merges, where an aligned merge is a pair of groups sharing
 * a common neighbour, not necessarily in the same container.
 *
 * @author robmoffat
 */
class PerpendicularAlignedMergeGenerator(
    gp: GroupPhase,
    ms: BasicMergeState,
    grouper: GeneratorBasedGroupingStrategy
) : AbstractAlignedMergeGenerator(
    gp, ms, grouper, false
) {

    override fun getMyBestPriority(): Int {
        return AbstractRuleBasedGroupingStrategy.PERP_ALIGNED
    }

    override fun getCode(): String {
        return "ContainerPerpAligned"
    }

    override fun processPossibleAligningGroups(g: GroupPhase.Group, d: Direction, lp: LinkProcessor) {
        g.processAllLeavingLinks(true, g.linkManager.allMask(), lp)
    }

    override fun getAlignmentDirections(g1: GroupPhase.Group): Set<Direction?> {
        val state = getState(g1)
        return when (state) {
            MergePlane.UNKNOWN -> NONE
            MergePlane.X_FIRST_MERGE -> UP_DOWN
            MergePlane.Y_FIRST_MERGE -> LEFT_RIGHT
        }
        throw LogicException("Unknown state: $state")
    }

    override fun processAlignedGroupsInAxis(
        alignedGroup: GroupPhase.Group, ms: BasicMergeState, axis: DirectedGroupAxis,
        mp: MergePlane, d: Direction?, lp: LinkProcessor
    ) {
        val mask = DirectedLinkManager.createMask(mp, true, false, d)
        alignedGroup.processAllLeavingLinks(true, mask, lp)
    }

    companion object {
        private val LEFT_RIGHT: Set<Direction?> = EnumSet.of(Direction.LEFT, Direction.RIGHT)
        private val UP_DOWN: Set<Direction?> = EnumSet.of(Direction.UP, Direction.DOWN)
        private val NONE: Set<Direction?> = emptySet()
    }
}