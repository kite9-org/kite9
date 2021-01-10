package org.kite9.diagram.visualization.planarization.rhd.grouping;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;

/**
 * Returns details of the state of the merge, and the grouping of each container.
 * A "live" group is one which is able to be merged (as opposed to already merged).
 */
public abstract class GroupResult {

	public GroupResult() {
		super();
	}

	protected Map<Container, ContainerStateInfo> containerStates;

	public abstract Collection<Group> groups();

	public ContainerStateInfo getStateFor(Container c) {
		return containerStates.get(c);
	}

	public class ContainerStateInfo {

		public Set<Group> contents;

		public Set<Container> incompleteSubcontainers;

		public boolean done = false;

		public ContainerStateInfo(Container c) {
			contents = new LinkedHashSet<Group>(c.getContents().size() * 2);
			incompleteSubcontainers = new UnorderedSet<Container>(4);
			containerStates.put(c, this);
		}

	}

}