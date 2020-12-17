package org.kite9.diagram.visualization.planarization.rhd.layout;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.algorithms.ssp.PriorityQueue;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;

/**
 * Modifies the queue so that we layout the groups with the most disparate group of links first.  
 * This is based on a count of the number of groups that are already placed that the candidate groups link to.
 */
public class MostNetworkedFirstLayoutQueue implements LayoutQueue, Logable {
	
	Kite9Log log = new Kite9Log(this);

	static class NetworkedItem {
		
		private int size;
		private Group g;
		
		public int getSize() {
			return size;
		}

		public Group getGroup() {
			return g;
		}

		public NetworkedItem(Group g, int size) {
			this.size = size;
			this.g = g;
		}

		@Override
		public String toString() {
			return "NI: "+g.getGroupNumber()+" size = "+getSize();
		}
		
		
		
		
	}
	
	public MostNetworkedFirstLayoutQueue(int size) {
		super();
		networkSizes = new HashMap<Group, Integer>(size*2);
		completedGroups = new UnorderedSet<Group>(size*2);
		todo = new PriorityQueue<NetworkedItem>(size, new Comparator<NetworkedItem>() {

			/**
			 * Although priority is top down, within a given level, do groups in the same order than they were merged
			 * in. This means that we do "hub" groups before "edge" ones.
			 */
			@Override
			public int compare(NetworkedItem arg0, NetworkedItem arg1) {
				// most networked first
				int a0d = arg0.getSize();
				int a1d = arg1.getSize();
				if (a0d != a1d) {
					return -((Integer) a0d).compareTo(a1d);
				}
				
				// largest first
				a0d = arg0.getGroup().getSize();
				a1d = arg1.getGroup().getSize();
				if (a0d != a1d) {
					return -((Integer) a0d).compareTo(a1d);
				}

				// lowest number first
				int a0n = arg0.getGroup().getGroupNumber();
				int a1n = arg1.getGroup().getGroupNumber();

				return ((Integer) a0n).compareTo(a1n);
			}

		});
	}

	public void offer(Group item) {
		if (canLayout(item)) {
			int liveGroupLinkCount = 0;
			LinkManager lm = item.getLinkManager();
			log.send(log.go() ? null : "Counting Network size for "+item.getGroupNumber());
			Collection<LinkDetail> links = lm.subset(lm.allMask());
			for (LinkDetail ld : links) {
				liveGroupLinkCount += countLinkNetworkSize(ld);
			}
			networkSizes.put(item, liveGroupLinkCount);
			NetworkedItem ni = new NetworkedItem(item, liveGroupLinkCount);
			todo.add(ni);
			log.send("Created: "+ni);
		}
	}

	private boolean canLayout(Group item) {
		return item.getAxis().isReadyToPosition(completedGroups);
	}

	private int countLinkNetworkSize(final LinkDetail ld) {
		boolean complete = completedGroups.contains(ld.getGroup());
		
		if (complete) {
			// drill down looking for first ready group
			final int out[] = {0};
			ld.processToLevel(new LinkProcessor() {
				
				@Override
				public void process(Group originatingGroup, Group destinationGroup, LinkDetail ld2) {
					out[0] += countLinkNetworkSize(ld2);
				}
			}, 1);
			
			return out[0];
		} else {
			log.send(log.go() ? null : " -- link to "+ld.getGroup());
			return 1;
		}
	}

	PriorityQueue<NetworkedItem> todo;
	Map<Group, Integer> networkSizes;
	Set<Group> completedGroups;
	Set<Group> groupsToPlace;
	

	@Override
	public Group poll() {
		while (todo.size() > 0) {
			NetworkedItem nw = todo.poll();
			Group out = nw.getGroup();
			networkSizes.remove(out);
			if (!completedGroups.contains(out)) {
				out.getLinkManager().setLinkCount(nw.getSize());
				return out;
			}
		}
		
		return null;
	}

	/**
	 * Updates (by creating new NetworkedItems) the groups currently in todo.
	 */
	@Override
	public void complete(CompoundGroup item) {
		completedGroups.add(item);
		Group a = item.getA();
		Group b = item.getB();
		boolean horiz = item.getAxis().isHorizontal();
		Collection<LinkDetail> links = item.getLinkManager().subset(item.getLinkManager().allMask());
		for (LinkDetail ld : links) {
			Group group = ld.getGroup();
			checkAndIncrementGroup(group, a, b, ld, horiz);
		}
	}

	private void checkAndIncrementGroup(Group group, final Group a, final Group b, final LinkDetail ld, final boolean horiz) {
		if (completedGroups.contains(group)) {
			if (group instanceof CompoundGroup) {
				// need to work our way down to incomplete ones, no point updating complete groups
				ld.processToLevel(new LinkProcessor() {
					
					@Override
					public void process(Group originatingGroup, Group destinationGroup, LinkDetail ld2) {
						checkAndIncrementGroup(destinationGroup, a, b, ld2, horiz);
					}
				}, 1);
			}
		} else {
			group = getGroupBeingLaidOut(group, horiz);
			if ((group != null) && (ld.from(a)) && (ld.from(b))) {
				Integer existingLinks = safeGet(group);
				networkSizes.put(group, existingLinks+1);
				NetworkedItem ni = new NetworkedItem(group, existingLinks+1);
				todo.add(ni);
				log.send("Bumped priority: "+ni);
			}	
		}
	}

	private Group getGroupBeingLaidOut(Group group, boolean horiz) {
		while (!canLayout(group)) {
			if (completedGroups.contains(group)) {
				return null;
			}
			
			group = group.getAxis().getParentGroup(horiz);
		}
		
		return group;
	}

	private Integer safeGet(Group group) {
		Integer out = networkSizes.get(group);
		if (out == null) {
			return 0;
		}
		
		return out;
	}

	@Override
	public String getPrefix() {
		return "MNFQ";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

}
