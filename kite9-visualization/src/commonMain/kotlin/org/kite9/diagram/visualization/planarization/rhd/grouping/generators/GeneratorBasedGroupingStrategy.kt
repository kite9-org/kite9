package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.MergeOption

interface GeneratorBasedGroupingStrategy : GroupingStrategy {
    /**
     * Quick compatibility check on two groups to make sure we are comparing like with like
     */
    fun compatibleMerge(a: Group, b: Group): Boolean

    /**
     * Allows generators to add merge options back to the merge state.
     * @return priority
     */
    fun addMergeOption(
        g1: Group,
        g2: Group,
        alignedGroup: Group?,
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
    fun getWorkingGroup(group: Group?): Group?
}