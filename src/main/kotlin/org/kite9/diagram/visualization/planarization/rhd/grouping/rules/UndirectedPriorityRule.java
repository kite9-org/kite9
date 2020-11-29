package org.kite9.diagram.visualization.planarization.rhd.grouping.rules;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.ContainerMergeType;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.PriorityRule;

public class UndirectedPriorityRule implements PriorityRule {

	@Override
	public int getMergePriority(Group a, Group b, DirectedMergeState ms,
			Group alignedGroup, Direction alignedSide, MergePlane axis,
			boolean horizontalMergesFirst) {
		boolean aDirectedLeavers = hasDirectedLeaversInContainer(a, axis, ms);
		boolean bDirectedLeavers = hasDirectedLeaversInContainer(b, axis, ms); 
		
		if (!aDirectedLeavers || !bDirectedLeavers) {
			return getPriority(a, b, alignedGroup, ms);
		}

		return UNDECIDED;

	}

	private boolean hasDirectedLeaversInContainer(Group a, MergePlane axis,
			BasicMergeState ms) {
		int mask = DirectedLinkManager.createMask(axis, true, true,
				Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
		return a.getLinkManager().subset(mask).size() > 0;
	}

	private int getPriority(Group a, Group b, Group alignedGroup, DirectedMergeState ms) {
		ContainerMergeType mt = ms.getContainerMergeType(a, b);
		if (a.getLink(b) != null) {
			return AbstractRuleBasedGroupingStrategy.UNDIRECTED_LINKED + mt.getPriorityAdjustment();
		} else if (alignedGroup != null) {
			return AbstractRuleBasedGroupingStrategy.UNDIRECTED_ALIGNED + mt.getPriorityAdjustment();
		} else {
			return AbstractRuleBasedGroupingStrategy.UNCONNECTED_NEIGHBOUR + mt.getPriorityAdjustment();
		}			
	}

}
