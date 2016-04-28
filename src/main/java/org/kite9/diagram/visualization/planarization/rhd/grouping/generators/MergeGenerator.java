package org.kite9.diagram.visualization.planarization.rhd.grouping.generators;

import org.kite9.diagram.primitives.Container;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;

/** 
 * Generates merges of a particular type, from a particular seed group.
 * Note that generators should not understand the precise logic of what constitutes a valid
 * merge, that is left to the priority rules.
 */
public interface MergeGenerator {

	/**
	 * With the given strategy, generate some merge options for the given group.
	 */
	public void generate(Group g);
	
	
	/**
	 * Signals that a container has become live.
	 */
	public void containerIsLive(Container c);

}
