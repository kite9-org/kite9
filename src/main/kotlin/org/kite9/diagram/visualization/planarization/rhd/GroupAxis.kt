package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

/**
 * Type allows us to store information about the group specific to the strategy being used to group.
 * Also this is used to write out the coordinates of the group for edge insertion, and figure out group position.
 *
 * @author robmoffat
 */
interface GroupAxis {
    /**
     * Determines whether this group is setting horizontal position
     */
    val isHorizontal: Boolean

    /**
     * Determines whether this group is setting vertical position
     */
    val isVertical: Boolean

    /**
     * Determine whether layout is needed at all for this group.
     */
    val isLayoutRequired: Boolean

    /**
     * Figures out where the group is, and returns routing info for it.
     * Set temp during the layout strategy phase to evaluate a layout before committing to it.
     */
    fun getPosition(rh: RoutableHandler2D, temp: Boolean): RoutingInfo

    /**
     * Tells the axis what group it is for.
     */
    fun setGroup(g: GroupPhase.Group)

    /**
     * Returns false if the group no longer needs combining into CompoundGroups
     */
    var active: Boolean

    /**
     * Returns true if this group's parents are all positioned, and we can choose the position of this one.
     */
    fun isReadyToPosition(completedGroups: Set<GroupPhase.Group>): Boolean

    /**
     * Potentially, groups can have 2 parents, a horizontal and a vertical one.
     */
    fun getParentGroup(horizontal: Boolean): CompoundGroup?
}