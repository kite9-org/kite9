package org.kite9.diagram.visualization.planarization.rhd.grouping.rules;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.ContainerMergeType;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedLinkManager;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.PriorityRule;
import org.kite9.framework.logging.LogicException;

public class AlignedDirectedPriorityRule implements PriorityRule {
	
	boolean axis;
	
	public AlignedDirectedPriorityRule(boolean axis) {
		this.axis = axis;
	}

	@Override
	public int getMergePriority(Group a, Group b, DirectedMergeState ms,
			Group alignedGroup, Direction alignedSide, MergePlane mp,
			boolean horizontalMergesFirst) {
		
		if (alignedGroup != null) {
			return canBuddyMerge(a, b, mp, alignedGroup, alignedSide, axis, ms);
		} else {
			return PriorityRule.UNDECIDED;
		}
	}

	private int getPriority(Group a, Group b, DirectedMergeState ms, BuddyType bt) {
		ContainerMergeType mt = ms.getContainerMergeType(a, b);
		if (axis) {
			switch (bt) {
			case HALF_BUDDY:
				return AbstractRuleBasedGroupingStrategy.AXIS_ALIGNED_UNSURE + mt.getPriorityAdjustment();
			case PERFECT_BUDDY:
				return AbstractRuleBasedGroupingStrategy.AXIS_ALIGNED+ mt.getPriorityAdjustment();			
			case NO:
			default:
				return UNDECIDED;
			}
		} else if (bt == BuddyType.PERFECT_BUDDY) {
			return AbstractRuleBasedGroupingStrategy.PERP_ALIGNED + mt.getPriorityAdjustment();
		} else {
			return UNDECIDED;
		}
	}

	

	protected int canBuddyMerge(Group group, Group buddy, MergePlane axis, Group alignedGroup, Direction alignedSide, boolean withAxis, BasicMergeState ms) {
		DirectedGroupAxis buddyAxis = DirectedGroupAxis.getType(buddy);
		DirectedGroupAxis groupAxis = DirectedGroupAxis.getType(group);

		if (((axis==MergePlane.Y_FIRST_MERGE) && withAxis) || ((axis==MergePlane.X_FIRST_MERGE) && !withAxis) || (axis==MergePlane.UNKNOWN)) {
			// left-right buddies
			boolean buddyMergeHorizontal = ((alignedSide == Direction.UP) || (alignedSide == Direction.DOWN)) 
					&& nearestNeighbours(alignedGroup, axis, alignedSide, group, buddy, ms);
			
			
			if (buddyMergeHorizontal && checkContainerLayoutSupportsAlignedMerge(group, buddy, alignedGroup, ms, alignedSide)) {
				BuddyType bt = areNearestNeighboursOrHaveNoNeighboursPerpendicular(groupAxis, group, buddyAxis, buddy, true, axis, ms);
				return getPriority(group, buddy, (DirectedMergeState) ms, bt);	
			} 
		} 
		
		if (((axis==MergePlane.X_FIRST_MERGE) && withAxis) || ((axis==MergePlane.Y_FIRST_MERGE) && !withAxis) || (axis==MergePlane.UNKNOWN)){
			// up-down buddies
			boolean buddyMergeVertical = ((alignedSide == Direction.LEFT) || (alignedSide == Direction.RIGHT)) && nearestNeighbours(alignedGroup, axis, alignedSide, group, buddy, ms);
			
			if (buddyMergeVertical && checkContainerLayoutSupportsAlignedMerge(group, buddy, alignedGroup, ms, alignedSide)) {
				BuddyType bt = areNearestNeighboursOrHaveNoNeighboursPerpendicular(groupAxis, group, buddyAxis, buddy, false, axis, ms);
				return getPriority(group, buddy, (DirectedMergeState) ms, bt);
			}
		} 
		
		return PriorityRule.UNDECIDED;
	}
	
	/**
	 * This makes sure that if you are doing a buddy merge, that the container will support it.
	 */
	private boolean checkContainerLayoutSupportsAlignedMerge(Group a, Group b,
		Group aligned, BasicMergeState ms, Direction alignedSide) {
	
		Set<Container> ac = ms.getContainersFor(a).keySet();
		Set<Container> bc = ms.getContainersFor(b).keySet();
		Set<Container> alignedc = ms.getContainersFor(aligned).keySet();
	
		for (Container con : alignedc) {
			if (ms.isContainerLive(con)) {
				// a and b sharing a container with alignedGroup will fail if con has a layout
				// REMOVE THIS?
				boolean conInB = bc.contains(con);
				boolean conInA = ac.contains(con);
//				if (conInB && conInA) {
//					Layout l = con.getLayoutDirection();
//					if ((l==Layout.HORIZONTAL) || (l==Layout.VERTICAL)) {
//						return false;
//					}
//				} 
				
				
				if ((conInA || conInB) && axis) {
					// container must allow alignedSide to occur
					Layout l = con.getLayout();
					if (!allowsMergeDirection(l, alignedSide)) {
						return false;
					}
				}
			}
		}
		
		
		// a and b must share a common live container, otherwise we are stretching containers
		for (Container c1 : ac) {
			if ((ms.isContainerLive(c1)) && (bc.contains(c1))) {
				return true;
			}
		}

		return false;
	}

	private boolean allowsMergeDirection(Layout l, Direction alignedSide) {
		if (l == null) {
			return true;
		} else if (l==Layout.HORIZONTAL) {
			return (alignedSide==Direction.LEFT) || (alignedSide==Direction.RIGHT) || (alignedSide==null);
		} else if (l==Layout.VERTICAL) {
			return (alignedSide==Direction.UP) || (alignedSide==Direction.DOWN) || (alignedSide==null);
		} else {
			return false;
//			throw new LogicException("Wasn't expecting aligned merge in a directed container");
		}
	}

	private boolean nearestNeighbours(Group alignedGroup, MergePlane axis, Direction alignedSide, Group group, Group buddy, BasicMergeState ms) {
		Collection<Group> neighboursGroup = getNearestNeighbours(group, Direction.reverse(alignedSide), axis, ms);
		Collection<Group> neighboursBuddy = getNearestNeighbours(buddy, Direction.reverse(alignedSide), axis, ms);
		
		return (neighboursBuddy.size() == neighboursGroup.size()) && (neighboursBuddy.contains(alignedGroup)) && (neighboursGroup.contains(alignedGroup)) && 
				sameNeighbours(neighboursGroup, neighboursBuddy);
	}

	
	private Collection<Group> getNearestNeighbours(Group p, Direction d, MergePlane mp, BasicMergeState ms) {
		int mask = DirectedLinkManager.createMask(mp, true, !axis , d);
		return p.getLinkManager().subsetGroup(mask);
	}
	
	enum BuddyType { PERFECT_BUDDY, HALF_BUDDY, NO }
	
	private BuddyType areNearestNeighboursOrHaveNoNeighboursPerpendicular(
			DirectedGroupAxis groupAxis, Group group,
			DirectedGroupAxis buddyAxis, Group buddy, boolean horizontal,
			MergePlane mp, BasicMergeState ms) {
	
		Direction d1 = horizontal ? Direction.LEFT : Direction.UP;
		Direction d2 = horizontal ? Direction.RIGHT : Direction.DOWN;
		
		int maskd1 = DirectedLinkManager.createMask(mp, true, false, d1);
		int maskd2 = DirectedLinkManager.createMask(mp, true, false, d2);
		
		Collection<Group> nng1 = group.getLinkManager().subsetGroup(maskd1);
		Collection<Group> nng2 = group.getLinkManager().subsetGroup(maskd2);
		
		
		if ((nng1.contains(buddy)) || (nng2.contains(buddy))) {
			return BuddyType.PERFECT_BUDDY;
		}
		
		Collection<Group> nnb1 = buddy.getLinkManager().subsetGroup(maskd1);
		Collection<Group> nnb2 = buddy.getLinkManager().subsetGroup(maskd2);
		
		if (nng2.size() + nnb2.size() == 0){
			// no way these can be joined on the d2 side
			return BuddyType.PERFECT_BUDDY;
		}
		
		if (nng1.size() + nnb1.size() == 0) {
			// no way these could be joined on the d1 side
			return BuddyType.PERFECT_BUDDY;
		}
		
		if (sameNeighbours(nng1, nnb1)) {
			// since the neighbours are the same, they couldn't be joined to each other
			return BuddyType.PERFECT_BUDDY;
		}
		
		if (sameNeighbours(nng2, nnb2)) {
			// since the neighbours are the same, they couldn't be joined to each other
			return BuddyType.PERFECT_BUDDY;
		}
		
		if (noCommonNeighbours(nng1, nnb2) || (noCommonNeighbours(nng2, nnb1))) {
			return BuddyType.HALF_BUDDY;
		}
		
		return BuddyType.NO;
	}

	private boolean noCommonNeighbours(Collection<Group> a,
			Collection<Group> b) {
		return Collections.disjoint(a, b);
	}

	private boolean sameNeighbours(Collection<Group> a,
			Collection<Group> b) {
		return (a.size() == b.size()) && (a.containsAll(b));
	}
	
	private boolean checkContainerAllowsAlignedMerge(Container c, MergePlane alignedState) {
		// exclude merge options where the aligned merge is incompatible with the container
		if (c.getLayout() == Layout.HORIZONTAL) {
			if (alignedState == MergePlane.X_FIRST_MERGE) {
				return false;
			}
		} else if (c.getLayout() == Layout.VERTICAL) {
			if (alignedState == MergePlane.Y_FIRST_MERGE) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean checkSharedContainers(Set<Container> ac, Set<Container> bc, MergePlane mp) {
		if ((ac == null) || (bc == null))
			throw new LogicException("Group has no containers?");
		
		Set<Container> done = new UnorderedSet<Container>(ac.size() + bc.size());
		boolean cont = false;
		
		for (Container c1 : ac) {
			if (bc.contains(c1)) {
				done.add(c1);
				if (!checkContainerAllowsAlignedMerge(c1, mp)) {
					return false;
				}
			} else {
				cont = true;
			}
		}
		
		if (cont) {
			ac = getParentContainers(ac, done);
			bc = getParentContainers(bc, done);
			return checkSharedContainers(ac, bc, mp);
		} else {
			return true;
		}
	}

	private Set<Container> getParentContainers(Set<Container> ac, Set<Container> done) {
		Set<Container> out = new UnorderedSet<Container>(ac.size());
		for (Container c : ac) {
			if ((!done.contains(c)) && (c instanceof Connected)) {
				out.add(((Connected)c).getContainer());
			}
		}
		
		return out;
	}
	
}
