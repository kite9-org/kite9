package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.AbstractCompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
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
     * Returns false if the group no longer needs combining into CompoundGroups
     */
    var active: Boolean

    /**
     * Potentially, groups can have 2 parents, a horizontal and a vertical one.
     */
    fun getParentGroup(horizontal: Boolean): CompoundGroup?

    /**
     * Returns details of the kind of axis this is.  (DirectedGroupAxis specialises this)
     */
    fun getAxisType(): Any
}