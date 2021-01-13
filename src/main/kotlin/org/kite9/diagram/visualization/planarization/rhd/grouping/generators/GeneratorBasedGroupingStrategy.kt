package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.MergeOption

interface GeneratorBasedGroupingStrategy : GroupingStrategy {
    /**
     * Quick compatibility check on two groups to make sure we are comparing like with like
     */
    fun compatibleMerge(a: GroupPhase.Group, b: GroupPhase.Group): Boolean

    /**
     * Allows generators to add merge options back to the merge state.
     * @return priority
     */
    fun addMergeOption(
        g1: GroupPhase.Group,
        g2: GroupPhase.Group,
        alignedGroup: GroupPhase.Group?,
        alignedSide: Direction?,
        bestPriority: Int,
        ms: BasicMergeState
    )

    /**
     * Basically the same, but with an already-existing merge option.
     * @return priority
     */
    fun addMergeOption(mo: MergeOption, bestPriority: Int, ms: BasicMergeState): Int

    /**
     * Given a group, which may have been merged, return the currently active version of it.
     */
    fun getWorkingGroup(group: GroupPhase.Group?): GroupPhase.Group?
}