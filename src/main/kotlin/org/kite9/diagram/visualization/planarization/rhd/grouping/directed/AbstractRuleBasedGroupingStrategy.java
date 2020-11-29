package org.kite9.diagram.visualization.planarization.rhd.grouping.directed;

import java.util.Collection;
import java.util.List;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.AbstractGroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.diagram.logging.LogicException;

/**
 * This class encapsulates the rules about directional merging.
 * 
 * When groups have directional edges, they will need to be merged in both axes.  The DirectedGroupTypeState
 * stores the state of this.  
 * 
 * If directional merging is going on (X_FIRST, Y_FIRST), groups need to exhaust the single directed merges in a given 
 * direction first, before beginning the single directed merges in the other direction.
 * 
 * If all single directed merges are exhausted, groups may buddy merge.  That is, where two groups
 * have a common neighbour in the same direction, they can merge together, which means that the common neighbour
 * now has a single group to merge with.  
 * 
 * Where groups don't have directed edges leaving them (UNDIRECTED), they can merge happily with any group not doing X_FIRST or Y_FIRST
 * merging.
 * 
 * When all the directed edges are consumed and merged into single X_FIRST and Y_FIRST groups, those
 * are themselves merged together into a compound group (UNDIRECTED).
 * 
 * About Axis / Perpendicular:
 * 
 * Let's say we are doing Y-first merge.  The axis is vertical.  The axis part of the merge is about creating columns through the diagram
 * where things are aligned.   The perpendicular part is then about grouping those columns together.
 * The requirements are different in each case.  Axis merging is all about creating columns, so it's more interested in single directed merges.
 * However, perpendicular is all about keeping containers together, so priorities are based on whether you are completing a container or not.
 *
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractRuleBasedGroupingStrategy extends AbstractGroupingStrategy {

	public AbstractRuleBasedGroupingStrategy() {
		super();
	}

	protected Collection<Group> getNearestNeighbours(Group p, Direction d, MergePlane axis, BasicMergeState ms) {
		int mask = DirectedLinkManager.createMask(axis, true, false, d);
		return p.getLinkManager().subsetGroup(mask);
	}
	
	protected Collection<Group> getNearestNeighboursInContainer(Group p, Direction d, MergePlane axis, BasicMergeState ms) {
		int mask = DirectedLinkManager.createMask(axis, true, true, d);
		return p.getLinkManager().subsetGroup(mask);
	}
	
	// different types of merges get different priorities.
	public static int AXIS_SINGLE_NEIGHBOUR = 0;	
	public static int AXIS_ALIGNED = 1;
	public static int AXIS_MULTI_NEIGHBOUR = 2;
	public static int PERP_NEIGHBOUR= 3;
	public static int PERP_ALIGNED = 4;
	public static int AXIS_ALIGNED_UNSURE = 5;
	public static int UNDIRECTED_LINKED = 6;
	public static int UNDIRECTED_ALIGNED = 7;
	public static int UNCONNECTED_NEIGHBOUR = 8;
	

	@Override
	public int canGroupsMerge(Group a, Group b, BasicMergeState ms, Group alignedGroup, Direction alignedSide) {
		//log.send("Testing merge: \n\t"+a+"\n\t"+b+"\n\t"+alignedGroup+"\n\t"+alignedSide);
		alignedGroup = alignedGroup == null ? null : getWorkingGroup(alignedGroup, ms);
		
		int out = super.canGroupsMerge(a, b, ms, alignedGroup, alignedSide);
		if (out == INVALID_MERGE) {
			return out;
		}
	
		MergePlane plane = DirectedGroupAxis.getMergePlane(a, b);

		if ((plane == null) || alreadyMerged(a, plane) || alreadyMerged(b, plane)){
			// means that the groups are now incompatible.
			return INVALID_MERGE;
		}
		
		// if there is a contradiction, record it and quit
		boolean hasContradiction = checkContradiction(a, b, plane, ms);
		if (hasContradiction) {
			return ILLEGAL_PRIORITY;
		}
	
		// figure out if the axis is horizontal or vertical first.
		boolean horizontalMergesFirst = true;
		if ((DirectedGroupAxis.getType(a).state == MergePlane.Y_FIRST_MERGE)
				|| (DirectedGroupAxis.getType(b).state == MergePlane.Y_FIRST_MERGE)
				|| (plane == MergePlane.Y_FIRST_MERGE)) {
			horizontalMergesFirst = false;
		}

		for (PriorityRule pr : getRules((DirectedMergeState) ms)) {
			int p = pr.getMergePriority(a, b, (DirectedMergeState) ms, alignedGroup, alignedSide, plane, horizontalMergesFirst);
			if (p!=PriorityRule.UNDECIDED) {
				return p;
			} 
		}
		
		return ILLEGAL_PRIORITY;
	}
	
	private boolean alreadyMerged(Group b, MergePlane plane) {
		if (plane == MergePlane.X_FIRST_MERGE) {
			return (b.getAxis().getParentGroup(false) != null);
		} else if (plane == MergePlane.Y_FIRST_MERGE) {
			return (b.getAxis().getParentGroup(true) != null);
		} else {
			return false;
		}
		
		
	}

	protected abstract List<PriorityRule> getRules(DirectedMergeState ms);

	public Group getWorkingGroup(Group group, BasicMergeState ms) {
		if (group == null) {
			return null;
		}
		DirectedGroupAxis axis = null;
		Group parent = null;
		do {
			if (parent != null) {
				group = parent;
				parent = null;
			}

			axis = (DirectedGroupAxis) group.getAxis();
			switch (axis.state) {
			case X_FIRST_MERGE:
				parent = axis.vertParentGroup;
				break;
			case Y_FIRST_MERGE:
				parent = axis.horizParentGroup;
				break;
			default:
				if (axis.horizParentGroup != axis.vertParentGroup) {
					throw new LogicException("Not decided");
				}
				parent = axis.horizParentGroup;
			}

		} while (parent != null);
		return group;

	}
	
	private boolean checkContradiction(Group a, Group b, MergePlane axis, BasicMergeState ms) {
		int directedEdgesOnly = DirectedLinkManager.createMask(axis, false, false, Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
		LinkManager blm = b.getLinkManager();
		Collection<LinkDetail> subset = a.getLinkManager().subset(directedEdgesOnly);
		for (LinkDetail gald : subset) {
			LinkDetail gbld = blm.get(gald.getGroup());
			if ((gbld != null) && (gbld.getDirection()!=null) && (gbld.getDirection()!=gald.getDirection())) {
				return true;
			}
		}
		
		return false;
	}

	
}