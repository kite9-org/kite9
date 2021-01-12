package org.kite9.diagram.visualization.planarization.rhd.grouping.rules

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Direction.Companion.rotateAntiClockwise
import org.kite9.diagram.model.position.Direction.Companion.rotateClockwise
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.*

class NeighbourDirectedPriorityRule(val axis: Boolean) : PriorityRule {

    override fun getMergePriority(
        a: GroupPhase.Group,
        b: GroupPhase.Group,
        ms: DirectedMergeState,
        alignedGroup: GroupPhase.Group?,
        alignedSide: Direction?,
        mp: MergePlane,
        horizontalMergesFirst: Boolean
    ): Int {
        if (alignedGroup != null) {
            return PriorityRule.UNDECIDED
        }
        val ld = a.getLink(b)
        if (ld != null && ld.direction != null) {
            val d = ld.direction
            when (mp) {
                MergePlane.UNKNOWN -> {
                }
                MergePlane.X_FIRST_MERGE -> if (d === Direction.DOWN || d === Direction.UP) {
                    if (axis) {
                        return PriorityRule.UNDECIDED
                    }
                }
                MergePlane.Y_FIRST_MERGE -> if (d === Direction.LEFT || d === Direction.RIGHT) {
                    if (axis) {
                        return PriorityRule.UNDECIDED
                    }
                }
            }
            val mask = DirectedLinkManager.createMask(mp, true, !axis, d)
            val rmask = DirectedLinkManager.createMask(mp, true, !axis, reverse(d))
            val aGroups = a.linkManager.subsetGroup(mask)
            val bGroups = b.linkManager.subsetGroup(rmask)
            if (aGroups.contains(b) && bGroups.contains(a)) {
                if (aGroups.size == 1 && bGroups.size == 1) {
                    // single directed merge
                    return getPriority(a, b, ms, true)
                }
                if (isCompatibleSubgroup(a, b, aGroups, bGroups, mp, d)) {
                    return getPriority(a, b, ms, false)
                }
                return if (isCompatibleSubgroup(b, a, bGroups, aGroups, mp, reverse(d))) {
                    getPriority(a, b, ms, false)
                } else AbstractRuleBasedGroupingStrategy.INVALID_MERGE
            }
        }
        return PriorityRule.UNDECIDED
    }

    private fun isCompatibleSubgroup(
        a: GroupPhase.Group,
        b: GroupPhase.Group,
        aGroupsBack: Collection<GroupPhase.Group>,
        siblingsOfA: Collection<GroupPhase.Group>,
        mp: MergePlane,
        d: Direction?
    ): Boolean {
        if (aGroupsBack.size == 1) {
            // check for perpendicular neighbours
            val perp1 = rotateAntiClockwise(
                d!!
            )
            val perp2 = rotateClockwise(
                d
            )
            val aPerp1Size = getPerpSize(a, mp, perp1)
            val aPerp2Size = getPerpSize(a, mp, perp2)
            if (aPerp1Size == 0 && aGroupsBack.size == 1) {
                if (aPerp2Size == 0) {
                    return true
                }
            }
        }
        return false
    }

    private fun getPerpSize(
        a: GroupPhase.Group, mp: MergePlane,
        d: Direction
    ): Int {
        val perpMask = DirectedLinkManager.createMask(mp, false, true, d)
        val aPerpGroups = a.linkManager.subsetGroup(perpMask)
        return aPerpGroups.size
    }

    private fun getPriority(
        a: GroupPhase.Group,
        b: GroupPhase.Group,
        ms: DirectedMergeState,
        singleDirected: Boolean
    ): Int {
        val mt = ms.getContainerMergeType(a, b)
        return if (axis) {
            if (singleDirected) {
                AbstractRuleBasedGroupingStrategy.AXIS_SINGLE_NEIGHBOUR
            } else {
                AbstractRuleBasedGroupingStrategy.AXIS_MULTI_NEIGHBOUR + mt.priorityAdjustment
            }
        } else {
            AbstractRuleBasedGroupingStrategy.PERP_NEIGHBOUR + mt.priorityAdjustment
        }
    }
}