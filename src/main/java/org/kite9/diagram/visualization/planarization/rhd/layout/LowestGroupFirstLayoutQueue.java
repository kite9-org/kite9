package org.kite9.diagram.visualization.planarization.rhd.layout;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;

import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;

public class LowestGroupFirstLayoutQueue implements LayoutQueue {

	public LowestGroupFirstLayoutQueue(int size) {
		super();
		completedGroups = new UnorderedSet<Group>(size*2);
		todo = new PriorityQueue<Group>(size, new Comparator<Group>() {

			/**
			 * Although priority is top down, within a given level, do groups in the same order than they were merged
			 * in. This means that we do "hub" groups before "edge" ones.
			 */
			@Override
			public int compare(Group arg0, Group arg1) {
				// x groups before y
				if (arg0.getAxis().isHorizontal() != arg1.getAxis().isHorizontal()) {
					return ((Boolean) arg0.getAxis().isHorizontal()).compareTo(arg1.getAxis().isHorizontal());
				}

				// largest groups first
				int a0d = arg0.getSize();
				int a1d = arg1.getSize();
				if (a0d != a1d) {
					return -((Integer) a0d).compareTo(a1d);
				}

				int a0n = arg0.getGroupNumber();
				int a1n = arg1.getGroupNumber();

				return ((Integer) a0n).compareTo(a1n);
			}

		});
	}

	public void offer(Group item) {
		if (item.getAxis().isReadyToPosition(completedGroups)) {
			todo.offer(item);
		}
	}

	PriorityQueue<Group> todo;
	Set<Group> completedGroups;

	@Override
	public Group poll() {
		if (todo.size() == 0) {
			return null;
		}

		return todo.poll();
	}

	@Override
	public void complete(CompoundGroup item) {
		completedGroups.add(item);
	}

	
}
