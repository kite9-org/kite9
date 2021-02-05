package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor

/**
 * Looks for aligned merges, where an aligned merge is a pair of groups sharing
 * a common neighbour. Aligned merges should be in-container.
 *
 * @author robmoffat
 */
class ContainerUndirectedAlignedMergeGenerator(
    gp: GroupPhase,
    ms: BasicMergeState,
    grouper: GeneratorBasedGroupingStrategy
) : AbstractAlignedMergeGenerator(
    gp, ms, grouper, true
) {
    override fun getMyBestPriority(): Int {
        return AbstractRuleBasedGroupingStrategy.UNDIRECTED_ALIGNED
    }

    override fun getCode(): String {
        return "ContainerUndirectedAligned"
    }

    override fun processPossibleAligningGroups(g: Group, d: Direction, lp: LinkProcessor) {
        g.processAllLeavingLinks(true, g.linkManager.allMask(), lp)
    }

    override fun getAlignmentDirections(g1: Group): Set<Direction?> {
        return NULL
    }

    override fun processAlignedGroupsInAxis(
        alignedGroup: Group, ms: BasicMergeState, axis: DirectedGroupAxis,
        mp: MergePlane, d: Direction?, lp: LinkProcessor
    ) {
        val mask = DirectedLinkManager.createMask(mp, false, false, d)
        alignedGroup.processAllLeavingLinks(true, mask, lp)
    }

    companion object {
        private val NULL: Set<Direction?> = setOf(null)
    }
}