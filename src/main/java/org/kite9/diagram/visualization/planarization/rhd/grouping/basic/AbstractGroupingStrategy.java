package org.kite9.diagram.visualization.planarization.rhd.grouping.basic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.LeafGroup;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult.ContainerStateInfo;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState.GroupContainerState;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail;
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

/**
 * Merges groups together into a hierarchical structure for routing layout.
 * 
 * MergeOptions hold the options for merge in a priority queue. 
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractGroupingStrategy implements GroupingStrategy, Logable {

	public static final int ILLEGAL_PRIORITY = 1000;
	public static final int INVALID_MERGE = -1;
	public Kite9Log log = new Kite9Log(this);

	@Override
	public String getPrefix() {
		return "GS  ";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
	
	/**
	 * Actually does the merge suggested by the merge option, replacing the individual groups with a compound group.
	 */
	protected final void performMerge(GroupPhase gp, BasicMergeState ms, MergeOption mo) {
		if (mo.getPriority() >= ILLEGAL_PRIORITY) {
			log.error(log.go() ? null : "Merging: " + mo);
		} else {
			log.send(log.go() ? null : "Merging: " + mo);
		}
		CompoundGroup combined = createCompoundGroup(gp, ms, mo);
		if (combined != null) {
			doCompoundGroupInsertion(gp, ms, combined, false);
		}
	}

	protected void doCompoundGroupInsertion(GroupPhase gp, BasicMergeState ms, CompoundGroup combined, boolean skipContainerCompletionCheck) {
		updateContainers(combined, gp, ms);
		removeOldGroups(gp, ms, combined);
		handleReferences(combined, gp, ms);
		introduceCombinedGroup(gp, ms, combined);
		if (!skipContainerCompletionCheck) {
			checkGroupsContainersAreComplete(combined, gp, ms);
		}
	}
	
	/**
	 * See if the merging of this group completes any containers, and change the container states
	 * accordingly.
	 */
	protected void updateContainers(CompoundGroup group, GroupPhase gp, BasicMergeState ms) {
		Map<Container, GroupContainerState> containersA = ms.getContainersFor(group.getA());
		Map<Container, GroupContainerState> containersB = ms.getContainersFor(group.getB());
		Set<Container> combined = new LinkedHashSet<Container>(containersA.keySet());
		
		combined.addAll(containersB.keySet());
 		
 		for (Iterator<Container> iterator = combined.iterator(); iterator.hasNext();) {
 			Container container = (Container) iterator.next();
 			GroupContainerState aContained = containersA.get(container);
 			GroupContainerState bContained = containersB.get(container);
 			boolean contained = (aContained != null) && (aContained.hasContent()) || 
 					(bContained != null) && (bContained.hasContent());
 			
 			boolean isComplete = (aContained != null) && (aContained.isComplete()) || 
 					(bContained != null) && (bContained.isComplete());
 			
 			GroupContainerState out = GroupContainerState.get(contained, isComplete);
 				
			ms.addGroupContainerMapping(group, container, out);
		}
	}
	
	/**
	 * Performs the functions needed to add the new compound group to the merge state
	 */
	protected abstract void introduceCombinedGroup(GroupPhase gp, BasicMergeState ms, CompoundGroup combined);


	protected abstract CompoundGroup createCompoundGroup(GroupPhase gp, BasicMergeState ms, MergeOption mo);

	/**
	 * This works out how to handle references going to the components of a new compound group.
	 */
	protected void handleReferences(final CompoundGroup group, final GroupPhase gp, final BasicMergeState ms) {
		final Group a = group.getA();
		final Group b = group.getB();
		
		group.processAllLeavingLinks(true, group.getLinkManager().allMask(), new LinkProcessor() {
			
			@Override
			public void process(Group originatingGroup, Group to, LinkDetail ld) {
				if (to.isActive()) {
					to.getLinkManager().notifyMerge(group, a.isActive(), b.isActive());
				}
			}
		});		
	}
	
	/**
	 * Checks the groups that were part of the merge and removes them.
	 */
	protected abstract void removeOldGroups(GroupPhase gp, BasicMergeState ms, CompoundGroup combined);
	
	/**
	 * If the provided group doesn't need any more merging, remove it from the merge state, and potentially, promote
	 * it's container.
	 */
	protected void checkGroupsContainersAreComplete(Group group, GroupPhase gp, BasicMergeState ms) {
		Map<Container, GroupContainerState> containerMap = ms.getContainersFor(group);
		Set<Container> containers = containerMap.keySet();
		ArrayList<Container> containers2 = new ArrayList<Container>(containers);
		for (Container container : containers2) {
			GroupContainerState state = containerMap.get(container);
			if ((state!=null) && (!state.isComplete())) {	
				if (isContainerComplete(container, ms)) {
					completeContainer(gp, ms, container);
				}	
			}
		}
	}

	protected boolean isContainerComplete(Container c, BasicMergeState ms) {
		ContainerStateInfo csi = ms.getStateFor(c);
		if (csi.done)
			return true;

		
		if (csi.incompleteSubcontainers.size() > 0) {
			return false;
		}
		
		csi.done = (isContainerCompleteInner(c, ms));
		return csi.done;
	}
	
	protected abstract boolean isContainerCompleteInner(Container c, BasicMergeState ms);

	protected boolean isContainerMergeable(Container c, BasicMergeState ms) {
		ContainerStateInfo csi = ms.getStateFor(c);
		return (csi.incompleteSubcontainers.size() == 0);
	}
	
	protected void completeContainer(GroupPhase gp, BasicMergeState ms, Container c) {
		// ok, no need to merge this one - it needs removing from the list
		log.send(log.go() ? null : "Completed container: "+c);
		
		ContainerStateInfo csiChild = ms.getStateFor(c);
		csiChild.done = true;
		ms.removeLiveContainer(c);

		
		Container cc = ((Contained)c).getContainer();
		if (cc != null) {
			// push groups from this container into parent
			ContainerStateInfo csiParent = ms.getStateFor(cc);
			csiParent.incompleteSubcontainers.remove(c);
			List<Group> toIterate = new ArrayList<Group>(csiChild.contents);
			boolean promotionOK = true;
			
			for (Group g : toIterate) {
				promotionOK = checkGroupChangeContainer(ms, c, cc, g) && promotionOK;
			}

			if (promotionOK) {
			
				if (isContainerMergeable(cc, ms)) {
					startContainerMerge(ms, cc);
				} 
				
				if (isContainerComplete(cc, ms)) {
					completeContainer(gp, ms, cc);
				}
				
			}

		} 
		
	}

	protected void startContainerMerge(BasicMergeState ms, Container c) {
		ms.addLiveContainer(c);
		ContainerStateInfo csi = ms.getStateFor(c);
		for (Group g : csi.contents) {
			ms.addLiveGroup(g);
			groupChangedContainer(ms, g);
		}
	}

	private boolean checkGroupChangeContainer(BasicMergeState ms, Container c, Container cc, Group g) {
		log.send("Moving group: "+g.getGroupNumber()+" from "+c+" to "+cc);
		ms.removeGroupContainerMapping(g, c);
		ms.addGroupContainerMapping(g, cc, GroupContainerState.HAS_CONTENT);
		return true;
	}

	protected abstract void groupChangedContainer(BasicMergeState ms, Group g);

	protected void initContained(GroupPhase gp, BasicMergeState ms, List<LeafGroup> leaves, LeafGroup toAdd) {		
		// set up container details
		Container c2 = toAdd.getContainer();
		if (c2 != null) {
			ms.addGroupContainerMapping(toAdd, c2, 
					(toAdd.getContained() != null) ? GroupContainerState.HAS_CONTENT : GroupContainerState.NO_CONTENT);
		}

		if (leaves!=null) {
			leaves.add(toAdd);
		}
	}
	
	/**
	 * Throws out any merge options that don't make sense. 
	 * 
	 * Priority is returned, or null if the merge is irrelevant
	 * 
	 * This is called before we add merge options to the queue, and also when we take them 
	 * off the queue, as the state will change.
	 * 
	 * Extra parameters about aligned group are added to simplify the process for 
	 */
	public int canGroupsMerge(Group a, Group b, BasicMergeState ms, Group alignedGroup, Direction d) {
		// not a real merge
		if (a == b) {
			return INVALID_MERGE;
		}

		return 0;
	}

	protected void preMergeInitialisation(GroupPhase gp, BasicMergeState ms) {
		for (LeafGroup g : gp.allGroups) {
			initContained(gp, ms, null, g);
		}
				
		List<Container> bottomLevelContainers = new ArrayList<Container>(ms.getContainers());
		
		if (bottomLevelContainers.size() > 0) {
			for (Container c : bottomLevelContainers) {
				if (isContainerMergeable(c, ms)) {
					startContainerMerge(ms, c);
				}
				if (isContainerComplete(c, ms)) {				
					completeContainer(gp, ms, c);
				} 
			}
		} else if (gp.allGroups.size() == 1){
			ms.addLiveGroup(gp.allGroups.iterator().next());
		}
	}
}
