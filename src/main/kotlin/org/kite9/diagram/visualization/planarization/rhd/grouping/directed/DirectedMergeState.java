package org.kite9.diagram.visualization.planarization.rhd.grouping.directed;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.framework.logging.Kite9Log;

public class DirectedMergeState extends BasicMergeState {
		
	public DirectedMergeState(ContradictionHandler ch) {
		super(ch);
	}

	@Override
	protected boolean isLinkCounted(Group from, Group to, Group via, LinkDetail ld) {
		if (DirectedGroupAxis.compatibleNeighbour(from, via)) {
			return super.isLinkCounted(from, to, via, ld);
		} else {
			return false;
		}
	}
	
	public ContainerMergeType getContainerMergeType(Group a, Group b) {
		boolean common = hasCommonLiveContainer(a, b);
		boolean increases = increasesContainers(a, b);
		if (common) {
			return increases ? ContainerMergeType.JOINING_EXTRA_CONTAINERS : ContainerMergeType.WITHIN_LIVE_CONTAINER;
		} else {
			return ContainerMergeType.NO_LIVE_CONTAINER;
		}
	}

	public boolean hasCommonLiveContainer(Group a, Group b) {
		Set<Container> ac = getContainersFor(a).keySet();
		Set<Container> bc = getContainersFor(b).keySet();
		Set<Container> itc = ac.size() < bc.size() ? ac : bc;
		Set<Container> inc = ac.size() < bc.size() ? bc : ac;

		for (Container container : itc) {
			if (inc.contains(container) && isContainerLive(container))
				return true;
		}

		return false;
	}

	/**
	 * If the resulting group is going to end up with more live containers than a or b individually,
	 * return true.
	 */
	public boolean increasesContainers(Group a,
			Group b) {
		Map<Container, GroupContainerState> ac = getContainersFor(a);
		Map<Container, GroupContainerState> bc = getContainersFor(b);
		
		return hasDifferentContainers(ac, bc) && hasDifferentContainers(bc, ac);
	}

	private boolean hasDifferentContainers(Map<Container, GroupContainerState> ac, Map<Container, GroupContainerState> bc) {
		for (Container container : ac.keySet()) {
			if (bc.get(container) == null) {
				return true;
			}
		}
		
		return false;
	}
	
	public static class ShapeIndex {
		
		private Group g;
		
		public ShapeIndex(Group g) {
			this.g = g;
		}

		@Override
		public boolean equals(Object arg0) {
			if (arg0 instanceof ShapeIndex) {
				// first, check for group equality.
				if (this.g == ((ShapeIndex)arg0).g) {
					return true;
				} else {
					// otherwise, see if the groups are equal because they contain the same leaves.
					return g.getLeafList().equals(((ShapeIndex) arg0).g.getLeafList());
				}
			} else {
				return false;
			}
		}

		

		@Override
		public int hashCode() {
			return g.hashCode();
		}
	}
	
	private Map<ShapeIndex, Group> noDirectedMergeNeeded; // live groups based on a key of which leaf groups they contain

	public boolean completedDirectionalMerge(Group combined) {
		final MergePlane mp = DirectedGroupAxis.getState(combined);
		int mask = DirectedLinkManager.createMask(mp, false, false, Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT);
		Collection<LinkDetail> incomplete = combined.getLinkManager().subset(mask);
		return incomplete.size() == 0;
	}

	@Override
	public void initialise(int capacity, int containers, Kite9Log log) {
		super.initialise(capacity, containers, log);
		noDirectedMergeNeeded = new HashMap<ShapeIndex, GroupPhase.Group>(capacity);
	}

	@Override
	public void removeLiveGroup(Group a) {
		super.removeLiveGroup(a);
		if (completedDirectionalMerge(a)) {
			ShapeIndex i = new ShapeIndex(a);
			Group b = noDirectedMergeNeeded.get(i);
			if (b == a) {
				noDirectedMergeNeeded.remove(i);
			}		
		}
	}
	
	public Group getCompoundGroupWithSameContents(Group g) {
		ShapeIndex toMatch = new ShapeIndex(g);
		Group out = noDirectedMergeNeeded.get(toMatch);
		return out;
	}

	@Override
	public void addLiveGroup(Group group) {
		super.addLiveGroup(group);
		if (completedDirectionalMerge(group)) {
			ShapeIndex i = new ShapeIndex(group);
			Group existing = noDirectedMergeNeeded.get(i);
			if (existing == null) {
				noDirectedMergeNeeded.put(i, group);
			}
		}
	}
	
	
	
	
}