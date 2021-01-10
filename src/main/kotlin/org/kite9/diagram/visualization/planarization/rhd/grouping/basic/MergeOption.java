package org.kite9.diagram.visualization.planarization.rhd.grouping.basic;

import org.kite9.diagram.common.hints.PositioningHints;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;

/**
 * Holds details about a potential merge. Best merges are done first.
 * Because merge options are used in priority queues, they should not be altered while
 * the object is in the queue.
 */
public class MergeOption implements Comparable<MergeOption> {

	public MergeKey mk;
	
	public int linkRank;

	public float linksIncluded;

	public float linksAligned;
	
	public Float planarDistance;
	
	public Float renderedDistance;
	
	public int alignmentGroupSize = Integer.MAX_VALUE;
	
	public int ordinalDistance = Integer.MAX_VALUE;
	
	public Group alignedGroup;
	
	public Direction alignedDirection;

	public float totalLinks;

	int size; // size of the groups, in terms of contained items subsumed
	
	int number;	// merge option number
	
	public int getNumber() {
		return number;
	}
	
	int priority = 0;

	/**
	 * Higher numbers indicate worse priority.  100 or more is illegal.
	 */
	public int getPriority() {
		return priority;
	}
	
	/**
	 * WARNING:  this should only be called if the merge option has been removed from the merge
	 * state.
	 */
	public void resetPriority(BasicMergeState ms, int p) {
		this.priority = p;
	}
	
	public MergeType getMergeType() {
		if (linksIncluded >= GroupPhase.LINK_WEIGHT) {
			return MergeType.LINKED;
		} else if (linksAligned > 0) {
			return MergeType.ALIGNED;
		} else {
			return MergeType.NEIGHBOUR;
		}
	}
	
	public MergeOption(Group a, Group b, int number, int p, Group alignedGroup, Direction alignedSide, BasicMergeState ms) {
		this.mk = new MergeKey(a, b);
		this.size = mk.getA().getSize() + mk.getB().getSize();
		this.number = number;
		this.priority = p;
		this.alignedDirection = alignedSide;
		this.alignedGroup = alignedGroup;
		if (alignedGroup != null) {
			alignmentGroupSize = alignedGroup.getSize();
		}
	}

	@Override
	public int compareTo(MergeOption arg0) {
		// order by priority first
		if (getPriority() != arg0.getPriority()) {
			return ((Integer)getPriority()).compareTo(arg0.getPriority());
		}
		
		// order by merge type first
		if (getMergeType() != arg0.getMergeType()) {
			return ((Integer)getMergeType().ordinal()).compareTo(arg0.getMergeType().ordinal());
		}
		
		switch (getMergeType()) {
		case LINKED: 
			// respect highest ranked links first.
			if ((arg0.linkRank != linkRank)) {
				return -((Integer) linkRank).compareTo(arg0.linkRank);
			}
			
			// we need t combine lowest-level stuff first
			if (arg0.size != size) {
				return ((Integer) size).compareTo(arg0.size);
			}

			// most value is from reducing total external links because they become internal
			if (arg0.linksIncluded != linksIncluded) {
				return -((Float) linksIncluded).compareTo(arg0.linksIncluded);
			}
			
			// finally, try to avoid merging two "hub" neighbour attr together
			if (totalLinks != arg0.totalLinks) {
				return ((Float) totalLinks).compareTo(arg0.totalLinks);
			}
			
			break;
			
		case ALIGNED:
			// join groups with smallest alignment group size (i.e. the group they both link to is smallest)
			if (alignmentGroupSize != arg0.alignmentGroupSize) {
				return ((Integer)alignmentGroupSize).compareTo(arg0.alignmentGroupSize);
			}

			// aligning links also reduces complexity of the overall graph, but not as much
			if (linksAligned != arg0.linksAligned) {
				return -((Float) linksAligned).compareTo(arg0.linksAligned);
			}
			
			// leave group with least non-aligned links
			if (totalLinks-linksAligned != arg0.totalLinks-linksAligned) {
				return ((Float) (totalLinks-linksAligned)).compareTo(arg0.totalLinks-arg0.linksAligned);
			}

			// we need to combine lowest-level stuff first
			if (arg0.size != size) {
				return ((Integer) size).compareTo(arg0.size);
			}
			

			break;
			
		default: // neighbour merge
			// merge together neighbours with least chance of being moved from the outside
			int thistl = Math.round(this.totalLinks);
			int arg0tl = Math.round(arg0.totalLinks);
			if (thistl != arg0tl) {
				return ((Integer)thistl).compareTo(arg0tl);
			}

			
			// merge closest neighbours first, to respect the ordering in the 
			// xml
			if (ordinalDistance != arg0.ordinalDistance) {
				return ((Integer)ordinalDistance).compareTo(arg0.ordinalDistance);
			}
			
			// try and merge smallest first, to achieve b-tree
			// and also to allow for more buddy merging
			if (arg0.size != size) {
				return ((Integer) size).compareTo(arg0.size);
			}
			
			break;
		}
		
		int dc = distanceCompare(this, arg0);

		if (dc != 0) {
			return dc;
		}
		
		return ((Integer)number).compareTo(arg0.number);
	}

	/**
	 * Looks at the distance 
	 * @param mergeOption
	 * @param arg0
	 * @return
	 */
	private int distanceCompare(MergeOption a, MergeOption b) {
		if ((a.planarDistance != null) && (b.planarDistance != null) && (a.planarDistance != b.planarDistance)) {
			return ((Float)a.planarDistance).compareTo(b.planarDistance);
		} else if ((a.renderedDistance != null) && (b.renderedDistance != null) && (a.renderedDistance != b.renderedDistance)) {
			return ((Float)a.renderedDistance).compareTo(b.renderedDistance);
		} else {
			return 0;
		}
	}

	public String toString() {
		return "[MO: " +number+" " + mk.getA().getGroupNumber() + " (" + mk.getA().getSize() + ")  " + mk.getB().getGroupNumber() + "(" + mk.getB().getSize() + "): t= "+getMergeType()+" i=" + linksIncluded + " a="
				+ linksAligned + " t=" + totalLinks + "ags="+alignmentGroupSize+" od="+ordinalDistance+", p="+priority+" a="+alignedGroup+" ad="+alignedDirection+" lr="+linkRank+"]";

	}

	/**
	 * Call this function on a merge option to figure out it's priority.
	 */
	public void calculateMergeOptionMetrics(BasicMergeState ms) {
		this.totalLinks = 0;
		this.linksAligned = 0;
		this.linksIncluded = 0;
		this.linkRank = 0;

		Group a = this.mk.getA();
		Group b = this.mk.getB();
		linkCount(a, b, this, ms);
		linkCount(b, a, this, ms);

		this.size = a.getSize() + b.getSize();		
		this.ordinalDistance = Math.abs(a.getGroupOrdinal() - b.getGroupOrdinal());
		this.planarDistance = PositioningHints.planarizationDistance(a.getHints(), b.getHints());
		this.renderedDistance = PositioningHints.positionDistance(a.getHints(), b.getHints());
	}

	private void linkCount(final Group group, final Group cand, final MergeOption mo, final BasicMergeState ms) {
		LinkDetail ldC = group.getLink(cand);
		if (ldC != null) {
			this.linksIncluded += ldC.getNumberOfLinks() / 2f;
			if (ldC.getDirection() != null) {
				this.linkRank = ldC.getLinkRank();
			}
		}
		
		this.totalLinks += group.getLinkManager().getLinkCount();
		
		if (this.alignedGroup != null) {
			LinkDetail ldA = group.getLink(this.alignedGroup);
			if (ldA != null) {
				this.linksAligned += ldA.getNumberOfLinks();
			}
		}
	}
	
}
