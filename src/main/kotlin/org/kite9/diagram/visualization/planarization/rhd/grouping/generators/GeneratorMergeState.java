package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.MergeOption;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedMergeState;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.PriorityRule;
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler;
import org.kite9.diagram.logging.Kite9Log;

/**
 * Extends the merge state for the generator based grouping strategy. Keeps a
 * list of which groups a generator has not been run on.
 * 
 * @author robmoffat
 * 
 */
public class GeneratorMergeState extends DirectedMergeState {

	public GeneratorMergeState(ContradictionHandler ch) {
		super(ch);
	}

	List<MergeGenerator> generators;

	List<PriorityRule> rules;

	PriorityQueue<Group> toDo;

	@Override
	public void initialise(int capacity, int containers, Kite9Log log) {
		super.initialise(capacity, containers, log);
		toDo = new PriorityQueue<Group>(capacity+1, new Comparator<Group>() {

			@Override
			public int compare(Group arg0, Group arg1) {
				if (arg0.getSize() != arg1.getSize()) {
					return ((Integer) arg0.getSize()).compareTo(arg1.getSize());
				} else {
					return ((Integer) arg0.getLinkManager().getLinkCount())
							.compareTo(arg1.getLinkManager().getLinkCount());
				}
			}

		});
	}

	public void setGenerators(List<MergeGenerator> generators) {
		this.generators = generators;
	}

	public void setRules(List<PriorityRule> rules) {
		this.rules = rules;
	}

	@Override
	public void addLiveGroup(Group group) {
		super.addLiveGroup(group);
		toDo.add(group);
	}

	public Group nextLiveGroup() {
		return toDo.poll();
	}

	public MergeOption getTopMerge() {
		return optionQueue.peek();
	}

}
