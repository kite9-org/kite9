package org.kite9.diagram.visualization.planarization.rhd.grouping.rules

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.*

class UndirectedPriorityRule : PriorityRule {

    override fun getMergePriority(
        a: GroupPhase.Group,
        b: GroupPhase.Group,
        ms: DirectedMergeState,
        alignedGroup: GroupPhase.Group?,
        alignedSide: Direction?,
        axis: MergePlane,
        horizontalMergesFirst: Boolean
    ): Int {
        val aDirectedLeavers = hasDirectedLeaversInContainer(a, axis, ms)
        val bDirectedLeavers = hasDirectedLeaversInContainer(b, axis, ms)
        return if (!aDirectedLeavers || !bDirectedLeavers) {
            getPriority(a, b, alignedGroup, ms)
        } else PriorityRule.UNDECIDED
    }

    private fun hasDirectedLeaversInContainer(
        a: GroupPhase.Group,
        axis: MergePlane,
        ms: BasicMergeState
    ): Boolean {
        val mask = DirectedLinkManager.createMask(
            axis, true, true,
            Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT
        )
        return a.linkManager.subset(mask).size > 0
    }

    private fun getPriority(
        a: GroupPhase.Group,
        b: GroupPhase.Group,
        alignedGroup: GroupPhase.Group?,
        ms: DirectedMergeState
    ): Int {
        val mt = ms.getContainerMergeType(a, b)
        return when {
            a.getLink(b) != null -> {
                AbstractRuleBasedGroupingStrategy.UNDIRECTED_LINKED + mt.priorityAdjustment
            }
            alignedGroup != null -> {
                AbstractRuleBasedGroupingStrategy.UNDIRECTED_ALIGNED + mt.priorityAdjustment
            }
            else -> {
                AbstractRuleBasedGroupingStrategy.UNCONNECTED_NEIGHBOUR + mt.priorityAdjustment
            }
        }
    }
}