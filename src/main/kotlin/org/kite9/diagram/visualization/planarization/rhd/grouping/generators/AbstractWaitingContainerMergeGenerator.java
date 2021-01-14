package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState.GroupContainerState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.MergeOption;
import org.kite9.diagram.logging.LogicException;

/**
 * Options that are not in live containers wait until the containers are live
 * before being added to the queue.
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractWaitingContainerMergeGenerator extends AbstractMergeGenerator {

	private boolean liveOnly = false;

	public boolean isLiveOnly() {
		return liveOnly;
	}

	public AbstractWaitingContainerMergeGenerator(GroupPhase gp, BasicMergeState ms,
			GeneratorBasedGroupingStrategy grouper, boolean liveOnly) {
		super(gp, ms, grouper);
		this.liveOnly = liveOnly;

		if (liveOnly) {
			waitingOptions = new HashMap<Container, Collection<MergeOption>>();
		}
	}

	private Map<Container, Collection<MergeOption>> waitingOptions;

	@Override
	public void containerIsLive(Container c) {
		if (isLiveOnly()) {
			Collection<MergeOption> waitList = waitingOptions.remove(c);
			if (waitList!=null) {
				for (MergeOption mo : waitList) {
					getGrouper().addMergeOption(mo, getMyBestPriority(), getMs());
				}
			}
		} 
	}
	
	public Container getCommonContainer(Group a, Group b) {
		Map<Container, GroupContainerState> ac = getMs().getContainersFor(a);
		Map<Container, GroupContainerState> bc = getMs().getContainersFor(b);
		

		if ((ac == null) || (bc == null))
			throw new LogicException("Group has no containers?");

		Set<Container> itc = ac.size() < bc.size() ? ac.keySet() : bc.keySet();
		Set<Container> inc = ac.size() < bc.size() ? bc.keySet() : ac.keySet();

		for (Container container : itc) {
			if (inc.contains(container))
				return container;
		}

		return null;
	}
	
	public void addMergeOption(Group g1, Group g2, Group alignedGroup, Direction alignedSide) {
		if (isLiveOnly()) {
			Container c = getCommonContainer(g1, g2);
			if (getMs().isContainerLive(c)) {
				super.addMergeOption(g1, g2, alignedGroup, alignedSide);
			} else {
				// add to the wait-list
				Collection<MergeOption> waitList = waitingOptions.get(c);
				if (waitList==null) {
					waitList = new ArrayList<MergeOption>();
					waitingOptions.put(c, waitList);
				}
				
				int p = getMyBestPriority();
				MergeOption mo = new MergeOption(g1, g2, getMs().nextMergeOptionNumber(), p, alignedGroup, alignedSide);
				waitList.add(mo); 
			}
		} else {
			super.addMergeOption(g1, g2, alignedGroup, alignedSide);
		}
	}
	
	
}
