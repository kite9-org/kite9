package org.kite9.diagram.visualization.planarization.rhd.layout;

import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;

public interface LayoutQueue {

	/**
	 * Adds another group to the work queue
	 */
	public void offer(Group item);

	/**
	 * Informs the queue that you have completed work on item.
	 */
	public void complete(CompoundGroup item);
	
	/**
	 * Retrieves the next group to do, or null if we're finished
	 */
	public Group poll();
	
	
}
