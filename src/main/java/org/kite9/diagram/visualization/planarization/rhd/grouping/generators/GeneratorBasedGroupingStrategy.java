package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;

import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.MergeOption;

public interface GeneratorBasedGroupingStrategy extends GroupingStrategy {

	/**
	 * Quick compatibility check on two groups to make sure we are comparing like with like 
	 */
	public boolean compatibleMerge(Group a, Group b);

	/**
	 * Allows generators to add merge options back to the merge state.
	 * @return priority
	 */
	public void addMergeOption(Group g1, Group g2, Group alignedGroup, Direction alignedSide, int bestPriority, BasicMergeState ms);

	/**
	 * Basically the same, but with an already-existing merge option.
	 * @return priority
	 */
	public int addMergeOption(MergeOption mo, int bestPriority, BasicMergeState ms);
	
	/**
	 * Given a group, which may have been merged, return the currently active version of it.
	 */
	public Group getWorkingGroup(Group group, BasicMergeState ms);
}
