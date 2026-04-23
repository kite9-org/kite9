package org.kite9.diagram.visualization.planarization.rhd.grouping.rules

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.*
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.merge.DirectedMergeState

class UndirectedPriorityRule : PriorityRule {

    override fun getMergePriority(
        a: Group,
        b: Group,
        ms: DirectedMergeState,
        alignedGroup: Group?,
        alignedSide: Direction?,
        axis: MergePlane,
        horizontalMergesFirst: Boolean
    ): Int {
        val aDirectedLeavers = hasDirectedLeaversInLiveContainer(a, axis, ms)
        val bDirectedLeavers = hasDirectedLeaversInLiveContainer(b, axis, ms)
        return if (!aDirectedLeavers || !bDirectedLeavers) {
            getPriority(a, b, alignedGroup, ms)
        } else PriorityRule.UNDECIDED
    }

    private fun hasDirectedLeaversInLiveContainer(
        a: Group,
        axis: MergePlane,
        ms: BasicMergeState
    ): Boolean {
        val mask = DirectedLinkManager.createMask(
            axis, true, true,
            Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT
        )

        fun groupInLiveContainer(g: Group) : Boolean {
            // if any part of what we connect to is not in a live container, then we
            // can't merge with it yet
            val containers = ms.getContainersFor(g)!!.keys.map { k -> ms.isContainerLive(k) }
            return !containers.contains(false)
        }

        val inContainerLeavers = a.linkManager.subsetGroup(mask)
        val inLiveContainerLeavers = inContainerLeavers.filter { groupInLiveContainer(it) }
        return inLiveContainerLeavers.isNotEmpty()
    }

    private fun getPriority(
        a: Group,
        b: Group,
        alignedGroup: Group?,
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