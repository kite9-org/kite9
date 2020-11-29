package org.kite9.diagram.visualization.planarization.rhd;

import java.util.Set;

import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

/**
 * Type allows us to store information about the group specific to the strategy being used to group.
 * Also this is used to write out the coordinates of the group for edge insertion, and figure out group position.
 * 
 * @author robmoffat
 *
 */
public interface GroupAxis {

	/**
	 * Determines whether this group is setting horizontal position
	 */
	public boolean isHorizontal();
	
	/**
	 * Determines whether this group is setting vertical position
	 */
	public boolean isVertical();
	
	/**
	 * Determine whether layout is needed at all for this group.
	 */
	public boolean isLayoutRequired();
	
	/**
	 * Figures out where the group is, and returns routing info for it.
	 * Set temp during the layout strategy phase to evaluate a layout before committing to it.
	 */
	public RoutingInfo getPosition(RoutableHandler2D rh, boolean temp);

	/**
	 * Tells the axis what group it is for.
	 */
	public void setGroup(Group g);

	/**
	 * Returns false if the group no longer needs combining into CompoundGroups
	 */
	public boolean isActive();

	/**
	 * Returns true if this group's parents are all positioned, and we can choose the position of this one.
	 */
	public boolean isReadyToPosition(Set<Group> completedGroups);
	
	/**
	 * Potentially, groups can have 2 parents, a horizontal and a vertical one.
	 */
	public CompoundGroup getParentGroup(boolean horizontal);
}
