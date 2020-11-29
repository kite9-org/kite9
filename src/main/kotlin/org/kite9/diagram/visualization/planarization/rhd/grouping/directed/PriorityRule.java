package org.kite9.diagram.visualization.planarization.rhd.grouping.directed;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;


/**
 * Checks to see if the particular combination of groups is allowed in the system in a given formation
 */
public interface PriorityRule {
	
	public static final int UNDECIDED = -1;

	public int getMergePriority(Group a, Group b, 
			DirectedMergeState ms, 
			Group alignedGroup, Direction alignedSide,
			MergePlane mp, 
			boolean horizontalMergesFirst);

}
