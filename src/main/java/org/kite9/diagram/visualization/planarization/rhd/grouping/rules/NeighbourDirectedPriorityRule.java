package org.kite9.diagram.visualization.planarization.rhd.grouping.rules;

import java.util.Collection;

import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.ContainerMergeType;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.PriorityRule;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;

public class NeighbourDirectedPriorityRule implements PriorityRule {

	boolean axis;
	
	public NeighbourDirectedPriorityRule(boolean axis) {
		this.axis = axis;
	}
	
	@Override
	public int getMergePriority(Group a, Group b, DirectedMergeState ms,
			Group alignedGroup, Direction alignedSide, MergePlane mp,
			boolean horizontalMergesFirst) {
		
		if (alignedGroup!=null) {
			return UNDECIDED;
		}
		
		LinkDetail ld = a.getLink(b);
		
		if ((ld != null) && (ld.getDirection()!=null)) {
			Direction d = ld.getDirection();
			
			// check direction
			switch (mp) {
			case UNKNOWN:
				break;
			case X_FIRST_MERGE:
				if ((d==Direction.DOWN) || (d==Direction.UP)) {
					if (axis) {
						return PriorityRule.UNDECIDED;
					} 				
				}
				break;
			case Y_FIRST_MERGE:
				if ((d==Direction.LEFT) || (d==Direction.RIGHT)) {
					if (axis) {
						return PriorityRule.UNDECIDED;
					} 	
				}
				break;
			}
			
			int mask = DirectedLinkManager.createMask(mp, true, !axis, d);
			int rmask = DirectedLinkManager.createMask(mp, true, !axis, Direction.reverse(d));
			Collection<Group> aGroups = a.getLinkManager().subsetGroup(mask);
			Collection<Group> bGroups = b.getLinkManager().subsetGroup(rmask);
			
			if ((aGroups.contains(b)) && (bGroups.contains(a))) {
				if ((aGroups.size()==1) && (bGroups.size()==1)) {
					// single directed merge
					return getPriority(a, b, ms, true);
				}
				
				if (isCompatibleSubgroup(a, b, aGroups, bGroups, mp, d)) {
					return getPriority(a, b, ms, false);
				}
				
				if (isCompatibleSubgroup(b, a, bGroups, aGroups, mp, Direction.reverse(d))) {
					return getPriority(a, b, ms, false);
				}
				
				
				return AbstractRuleBasedGroupingStrategy.INVALID_MERGE;
			}
		}
		return PriorityRule.UNDECIDED;
	}

	private boolean isCompatibleSubgroup(Group a, Group b, Collection<Group> aGroupsBack,
			Collection<Group> siblingsOfA, MergePlane mp, Direction d) {
		
		if (aGroupsBack.size()==1) {
			// check for perpendicular neighbours
			Direction perp1 = Direction.rotateAntiClockwise(d);
			Direction perp2 = Direction.rotateClockwise(d);
			int aPerp1Size = getPerpSize(a, mp, perp1);
			int aPerp2Size = getPerpSize(a, mp, perp2);
	
			
			if ((aPerp1Size == 0) && (aGroupsBack.size() == 1)) {
				if (aPerp2Size == 0) {
					return true;
				}
			}
		}

		return false;
		
	}


	private int getPerpSize(Group a, MergePlane mp,
			Direction d) {

		int perpMask = DirectedLinkManager.createMask(mp, false, true,d);
		Collection<Group> aPerpGroups = a.getLinkManager().subsetGroup(perpMask);
		return aPerpGroups.size();
	}

	private int getPriority(Group a, Group b, DirectedMergeState ms, boolean singleDirected) {
		ContainerMergeType mt = ms.getContainerMergeType(a, b);
		if (axis) {
			if (singleDirected) {
				return AbstractRuleBasedGroupingStrategy.AXIS_SINGLE_NEIGHBOUR;	
			} else {
				return AbstractRuleBasedGroupingStrategy.AXIS_MULTI_NEIGHBOUR + mt.getPriorityAdjustment();
			}
		} else {
			return AbstractRuleBasedGroupingStrategy.PERP_NEIGHBOUR + mt.getPriorityAdjustment();
		}
	}
}
