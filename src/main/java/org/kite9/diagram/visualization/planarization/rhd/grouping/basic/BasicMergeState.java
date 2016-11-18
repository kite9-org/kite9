package org.kite9.diagram.visualization.planarization.rhd.grouping.basic;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.LeafGroup;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult;
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.LogicException;

public class BasicMergeState extends GroupResult {
	
	public enum GroupContainerState { HAS_CONTENT(true, false), COMPLETE_WITH_CONTENT(true, true), NO_CONTENT(false, false), COMPLETE_NO_CONTENT(false, true);
		
		boolean complete;
		
		public boolean isComplete() {
			return complete;
		}

		public boolean hasContent() {
			return hasContent;
		}

		boolean hasContent;
		
		private GroupContainerState(boolean hasContent, boolean isComplete) {
			this.complete = isComplete;
			this.hasContent = hasContent;
		}

		public static GroupContainerState get(boolean contained,
				boolean isComplete) {
			if (contained) {
				return isComplete ? COMPLETE_WITH_CONTENT : HAS_CONTENT;
			} else {
				return isComplete ? COMPLETE_NO_CONTENT : NO_CONTENT;
			}
		}
	}
	
	protected Set<Group> liveGroups;
	protected PriorityQueue<MergeOption> optionQueue; // options in processing order
	protected Map<MergeKey, MergeOption> bestOptions; // contains all considered
													// merge options, legal ones
													// outrank illegal ones
	private Map<Group, Map<Container, GroupContainerState>> groupContainers;
	protected Set<Container> liveContainers;
	protected int nextMergeNumber = 0;
	protected Kite9Log log;
	protected ContradictionHandler ch;

	public BasicMergeState(ContradictionHandler ch) {
		this.ch = ch;
	}
	
	public void initialise(int capacity, int containers, Kite9Log log) {
		optionQueue = new PriorityQueue<MergeOption>(capacity * 5);
		bestOptions = new HashMap<MergeKey, MergeOption>(capacity * 5);
		containerStates = new HashMap<Container, ContainerStateInfo>(containers * 2);
		groupContainers = new HashMap<Group, Map<Container, GroupContainerState>>(capacity);
		liveContainers = new UnorderedSet<Container>(containers * 2);
		liveGroups = new DetHashSet<Group>(capacity * 2);
		this.log = log;
	}

	public void removeLiveGroup(Group a) {
		log.send("Completed group: " + a);
		a.setLive(false);
		liveGroups.remove(a);
		
		for (Container c : groupContainers.get(a).keySet()) {
			ContainerStateInfo csi = containerStates.get(c);
			csi.contents.remove(a);
		}
		
	}
	
	public void removeLiveContainer(Container c) {
		liveContainers.remove(c);
	}

	public boolean addOption(MergeOption mo) {
		MergeOption existing = bestOptions.get(mo.mk);
		if ((existing == null) || (mo.compareTo(existing) == -1)) {
			// this option is better than existing
			optionQueue.add(mo);
			bestOptions.put(mo.mk, mo);
			//log.send("New Merge Option: " + mo);
			return true;
		}
		
		return false;
	}

	public MergeOption getBestOption(MergeKey mk) {
		return bestOptions.get(mk);
	}

	public int nextMergeOptionNumber() {
		return nextMergeNumber++;
	}

	public boolean isLiveGroup(Group group) {
		return group.isLive();
	}

	public void addLiveGroup(Group group) {
		liveGroups.add(group);
		group.setLive(true);
	}
	
	public void addGroupContainerMapping(Group toAdd, Container c2, GroupContainerState newState) {
		Map<Container, GroupContainerState> within = groupContainers.get(toAdd);
		if (within == null) {
			within = new HashMap<Container, GroupContainerState>(5);
			groupContainers.put(toAdd, within);
		}

		log.send("Mapping "+toAdd.getGroupNumber()+" into container "+c2+" state= "+newState);
		
		within.put(c2, newState);
		ContainerStateInfo csi = getStateFor(c2);
		csi.contents.add(toAdd);
	}
	
	public ContainerStateInfo getStateFor(Container c2) {
		ContainerStateInfo csi = super.getStateFor(c2);
		if (csi == null) {
			csi = new ContainerStateInfo(c2);
			super.containerStates.put(c2, csi);
			
			for (DiagramElement c : c2.getContents()) {
				if (c instanceof Container) {
					ContainerStateInfo csi2 = getStateFor((Container) c);
					if (csi2 != null) {
						csi.incompleteSubcontainers.add((Container) c);
					}
				}
			}
			
		}
		return csi;
	}

	
	public void addLiveContainer(Container c) {
		log.send("Making container live:"+c);
		liveContainers.add(c);
	}

	public int groupsCount() {
		return liveGroups.size();
	}

	public boolean hasRemainingMergeOptions() {
		return optionQueue.size() > 0;
	}

	public MergeOption nextMergeOption() {
		log.send("Merge options:", optionQueue);
		MergeOption mo = optionQueue.remove();
		bestOptions.remove(mo.mk);
		
		return mo;
	}

	protected boolean isLinkCounted(Group from, Group to, Group via, LinkDetail ld) {
		if (!via.isActive()) {
			return false;
		}
		
		return true;
	}

	
	public Collection<Container> getContainers() {
		return containerStates.keySet();
	}

	public Map<Container, GroupContainerState> getContainersFor(Group a) {
		if (groupContainers == null) {
			if (a instanceof LeafGroup) {
				return Collections.singletonMap(((LeafGroup)a).getContainer(), GroupContainerState.HAS_CONTENT);
			} else {
				throw new LogicException("Group Containers should have been initialised");
			}
		}
		return groupContainers.get(a);
	}

	public GroupContainerState removeGroupContainerMapping(Group g, Container c) {
		Map<Container, GroupContainerState> within = groupContainers.get(g);
		if (within == null) {
			throw new LogicException("Group not present in an existing container"+g);
		}

		return within.remove(c);
	}

	public boolean isContainerLive(Container container) {
		return liveContainers.contains(container);
	}
	
	@Override
	public Collection<Group> groups() {
		return liveGroups;
	}

	public ContradictionHandler getContradictionHandler() {
		return ch;
	}
}