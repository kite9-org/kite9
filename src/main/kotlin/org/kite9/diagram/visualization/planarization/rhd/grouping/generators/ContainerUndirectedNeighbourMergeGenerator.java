package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult.ContainerStateInfo;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane;

/**
 * Generates some merge options for mergable containers.  We store live groups in exactly 
 * the same way as the other generators, but instead of generating a single merge option
 * we generate all the ones needed to merge the whole container.   This is because it's quciker
 * to do this, and you're likely to need them.
 * 
 * @author robmoffat
 *
 */
public class ContainerUndirectedNeighbourMergeGenerator extends AbstractMergeGenerator {

	public ContainerUndirectedNeighbourMergeGenerator(GroupPhase gp, BasicMergeState ms, GeneratorBasedGroupingStrategy grouper) {
		super(gp, ms, grouper);
	}
	
	@Override
	public void containerIsLive(Container c) {
		super.containerIsLive(c);
		dontDo.put(c, new UnorderedSet<GroupPhase.Group>());
	}

	// keeps track of groups we've already done
	Map<Container, Set<Group>> dontDo = new HashMap<Container, Set<Group>>();

	private void generateNeighboursForContainer(Container c, BasicMergeState ms,
			GeneratorBasedGroupingStrategy grouper, MergePlane mp) {
		log.send(log.go() ? null : "Generating "+getCode()+" options for "+c+" in axis "+mp);
		ContainerStateInfo csi = ms.getStateFor(c);

		int contentCount = csi.getContents().size();
		Set<Group> myDontDo = dontDo.get(c);
		
		List<Group> orderedItems = new ArrayList<Group>(contentCount);
				
		for (Group group : csi.getContents()) {
			if (mp.matches(DirectedGroupAxis.getState(group))) {
				myDontDo.add(group);

//				if (!ms.isLiveGroup(group))
//					throw new LogicException("Container Undirected merge has broken");
	
				orderedItems.add(group);
			}
		}
		
		Collections.sort(orderedItems, new Comparator<Group>() {

			@Override
			public int compare(Group o1, Group o2) {
				return ((Integer)o1.getGroupOrdinal()).compareTo(o2.getGroupOrdinal());
			}
		});
		
		
		createNeighbourMergeOptions(gp, ms, orderedItems, grouper, c);
	}

	private void createNeighbourMergeOptions(GroupPhase gp, BasicMergeState ms, List<Group> orderedItems, GeneratorBasedGroupingStrategy grouper, Container c) {
		Group prev = null;
		for (int i = 0; i < orderedItems.size(); i++) {
			Group current = orderedItems.get(i);
			if ((current != null)) {
				if (prev != null) {
					addMergeOption(prev, current, null, null);
				} 
				prev = current;
			}
		}
	}

	@Override
	protected int getMyBestPriority() {
		return AbstractRuleBasedGroupingStrategy.UNCONNECTED_NEIGHBOUR;
	}


	@Override
	public void generate(Group poll) {
		for (Container c : ms.getContainersFor(poll).keySet()) {
			if ((ms.isContainerLive(c))) {
				if (dontDo.get(c).contains(poll)) {
					return;
				}
				MergePlane state = DirectedGroupAxis.getState(poll);
				switch (state) {
				case UNKNOWN :
					generateNeighboursForContainer(c, ms, grouper, MergePlane.X_FIRST_MERGE);
					generateNeighboursForContainer(c, ms, grouper, MergePlane.Y_FIRST_MERGE);	
					break;
				case X_FIRST_MERGE:
					generateNeighboursForContainer(c, ms, grouper, MergePlane.X_FIRST_MERGE);
					break;
			
				case Y_FIRST_MERGE:
					generateNeighboursForContainer(c, ms, grouper, MergePlane.Y_FIRST_MERGE);
					break;
				}				
			} else if (dontDo.containsKey(c)) {
				dontDo.remove(c);
			}
		}
	}



	@Override
	protected String getCode() {
		return "ContainerNeighbour";
	}
	
}
