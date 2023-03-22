package org.kite9.diagram.visualization.planarization.rhd.grouping.rules

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.*
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.merge.DirectedMergeState

class AlignedDirectedPriorityRule(val axis: Boolean) : PriorityRule {

    override fun getMergePriority(
        a: Group,
        b: Group,
        ms: DirectedMergeState,
        alignedGroup: Group?,
        alignedSide: Direction?,
        mp: MergePlane,
        horizontalMergesFirst: Boolean
    ): Int {
        return alignedGroup?.let { canBuddyMerge(a, b, mp, it, alignedSide, axis, ms) } ?: PriorityRule.UNDECIDED
    }

    private fun getPriority(a: Group, b: Group, ms: DirectedMergeState, bt: BuddyType): Int {
        val mt = ms.getContainerMergeType(a, b)
        return if (axis) {
            when (bt) {
                BuddyType.HALF_BUDDY -> AbstractRuleBasedGroupingStrategy.AXIS_ALIGNED_UNSURE + mt.priorityAdjustment
                BuddyType.PERFECT_BUDDY -> AbstractRuleBasedGroupingStrategy.AXIS_ALIGNED + mt.priorityAdjustment
                BuddyType.NO -> PriorityRule.UNDECIDED
                else -> PriorityRule.UNDECIDED
            }
        } else if (bt == BuddyType.PERFECT_BUDDY) {
            AbstractRuleBasedGroupingStrategy.PERP_ALIGNED + mt.priorityAdjustment
        } else {
            PriorityRule.UNDECIDED
        }
    }

    private fun canBuddyMerge(
        group: Group,
        buddy: Group,
        axis: MergePlane,
        alignedGroup: Group,
        alignedSide: Direction?,
        withAxis: Boolean,
        ms: BasicMergeState
    ): Int {
        if (axis === MergePlane.Y_FIRST_MERGE && withAxis || axis === MergePlane.X_FIRST_MERGE && !withAxis || axis === MergePlane.UNKNOWN) {
            // left-right buddies
            val buddyMergeHorizontal = ((alignedSide === Direction.UP || alignedSide === Direction.DOWN)
                    && nearestNeighbours(alignedGroup, axis, alignedSide, group, buddy, ms))
            if (buddyMergeHorizontal && checkContainerLayoutSupportsAlignedMerge(
                    group,
                    buddy,
                    alignedGroup,
                    ms,
                    alignedSide
                )
            ) {
                val bt = areNearestNeighboursOrHaveNoNeighboursPerpendicular(
                    group,
                    buddy,
                    true,
                    axis
                )
                return getPriority(group, buddy, ms as DirectedMergeState, bt)
            }
        }
        if (axis === MergePlane.X_FIRST_MERGE && withAxis || axis === MergePlane.Y_FIRST_MERGE && !withAxis || axis === MergePlane.UNKNOWN) {
            // up-down buddies
            val buddyMergeVertical =
                (alignedSide === Direction.LEFT || alignedSide === Direction.RIGHT) && nearestNeighbours(
                    alignedGroup,
                    axis,
                    alignedSide,
                    group,
                    buddy,
                    ms
                )
            if (buddyMergeVertical && checkContainerLayoutSupportsAlignedMerge(
                    group,
                    buddy,
                    alignedGroup,
                    ms,
                    alignedSide
                )
            ) {
                val bt = areNearestNeighboursOrHaveNoNeighboursPerpendicular(
                    group,
                    buddy,
                    false,
                    axis,
                )
                return getPriority(group, buddy, ms as DirectedMergeState, bt)
            }
        }
        return PriorityRule.UNDECIDED
    }

    /**
     * This makes sure that if you are doing a buddy merge, that the container will support it.
     */
    private fun checkContainerLayoutSupportsAlignedMerge(
        a: Group, b: Group,
        aligned: Group, ms: BasicMergeState, alignedSide: Direction?
    ): Boolean {
        val ac: Set<Container> = ms.getContainersFor(a)!!.keys
        val bc: Set<Container> = ms.getContainersFor(b)!!.keys
        val alignedc: Set<Container> = ms.getContainersFor(aligned)!!.keys
        for (con in alignedc) {
            if (ms.isContainerLive(con)) {
                // a and b sharing a container with alignedGroup will fail if con has a layout
                // REMOVE THIS?
                val conInB = bc.contains(con)
                val conInA = ac.contains(con)
                if ((conInA || conInB) && axis) {
                    // container must allow alignedSide to occur
                    val l = con.getLayout()
                    if (!allowsMergeDirection(l, alignedSide)) {
                        return false
                    }
                }
            }
        }


        // a and b must share a common live container, otherwise we are stretching containers
        for (c1 in ac) {
            if (ms.isContainerLive(c1) && bc.contains(c1)) {
                return true
            }
        }
        return false
    }

    private fun allowsMergeDirection(l: Layout?, alignedSide: Direction?): Boolean {
        return if (l == null) {
            true
        } else if (l === Layout.HORIZONTAL) {
            alignedSide === Direction.LEFT || alignedSide === Direction.RIGHT || alignedSide == null
        } else if (l === Layout.VERTICAL) {
            alignedSide === Direction.UP || alignedSide === Direction.DOWN || alignedSide == null
        } else {
            false
        }
    }

    private fun nearestNeighbours(
        alignedGroup: Group,
        axis: MergePlane,
        alignedSide: Direction,
        group: Group,
        buddy: Group,
        ms: BasicMergeState
    ): Boolean {
        val neighboursGroup = getNearestNeighbours(group, reverse(alignedSide), axis, ms)
        val neighboursBuddy = getNearestNeighbours(buddy, reverse(alignedSide), axis, ms)
        return neighboursBuddy.size == neighboursGroup.size && neighboursBuddy.contains(alignedGroup) && neighboursGroup.contains(
            alignedGroup
        ) && sameNeighbours(neighboursGroup, neighboursBuddy)
    }

    private fun getNearestNeighbours(
        p: Group,
        d: Direction?,
        mp: MergePlane,
        ms: BasicMergeState
    ): Collection<Group> {
        val mask = DirectedLinkManager.createMask(mp, true, !axis, d)
        return p.linkManager.subsetGroup(mask)
    }

    internal enum class BuddyType {
        PERFECT_BUDDY, HALF_BUDDY, NO
    }

    private fun areNearestNeighboursOrHaveNoNeighboursPerpendicular(
        group: Group,
        buddy: Group,
        horizontal: Boolean,
        mp: MergePlane,
    ): BuddyType {
        val d1 = if (horizontal) Direction.LEFT else Direction.UP
        val d2 = if (horizontal) Direction.RIGHT else Direction.DOWN
        val maskd1 = DirectedLinkManager.createMask(mp, true, false, d1)
        val maskd2 = DirectedLinkManager.createMask(mp, true, false, d2)
        val nng1: Collection<Group> = group.linkManager.subsetGroup(maskd1)
        val nng2: Collection<Group> = group.linkManager.subsetGroup(maskd2)
        if (nng1.contains(buddy) || nng2.contains(buddy)) {
            return BuddyType.PERFECT_BUDDY
        }
        val nnb1: Collection<Group> = buddy.linkManager.subsetGroup(maskd1)
        val nnb2: Collection<Group> = buddy.linkManager.subsetGroup(maskd2)
        if (nng2.size + nnb2.size == 0) {
            // no way these can be joined on the d2 side
            return BuddyType.PERFECT_BUDDY
        }
        if (nng1.size + nnb1.size == 0) {
            // no way these could be joined on the d1 side
            return BuddyType.PERFECT_BUDDY
        }
        if (sameNeighbours(nng1, nnb1)) {
            // since the neighbours are the same, they couldn't be joined to each other
            return BuddyType.PERFECT_BUDDY
        }
        if (sameNeighbours(nng2, nnb2)) {
            // since the neighbours are the same, they couldn't be joined to each other
            return BuddyType.PERFECT_BUDDY
        }
        return if (noCommonNeighbours(nng1, nnb2) || noCommonNeighbours(nng2, nnb1)) {
            BuddyType.HALF_BUDDY
        } else BuddyType.NO
    }

    private fun noCommonNeighbours(
        a: Collection<Group>,
        b: Collection<Group>
    ): Boolean {
        return a.intersect(b).isEmpty()
    }

    private fun sameNeighbours(
        a: Collection<Group>,
        b: Collection<Group>
    ): Boolean {
        return a.size == b.size && a.containsAll(b)
    }

    private fun checkContainerAllowsAlignedMerge(c: Container?, alignedState: MergePlane): Boolean {
        // exclude merge options where the aligned merge is incompatible with the container
        if (c!!.getLayout() === Layout.HORIZONTAL) {
            if (alignedState === MergePlane.X_FIRST_MERGE) {
                return false
            }
        } else if (c!!.getLayout() === Layout.VERTICAL) {
            if (alignedState === MergePlane.Y_FIRST_MERGE) {
                return false
            }
        }
        return true
    }

    fun checkSharedContainers(ac: Set<Container?>?, bc: Set<Container?>?, mp: MergePlane): Boolean {
        var ac = ac
        var bc = bc
        if (ac == null || bc == null) throw LogicException("Group has no containers?")
        val done: MutableSet<Container?> = UnorderedSet(ac.size + bc.size)
        var cont = false
        for (c1 in ac) {
            if (bc.contains(c1)) {
                done.add(c1)
                if (!checkContainerAllowsAlignedMerge(c1, mp)) {
                    return false
                }
            } else {
                cont = true
            }
        }
        return if (cont) {
            ac = getParentContainers(ac, done)
            bc = getParentContainers(bc, done)
            checkSharedContainers(ac, bc, mp)
        } else {
            true
        }
    }

    private fun getParentContainers(ac: Set<Container?>, done: Set<Container?>): Set<Container?> {
        val out: MutableSet<Container?> = UnorderedSet(ac.size)
        for (c in ac) {
            if (!done.contains(c) && c is ConnectedRectangular) {
                out.add((c as ConnectedRectangular).getContainer())
            }
        }
        return out
    }
}