package org.kite9.diagram.visualization.planarization.rhd.grouping;

import org.kite9.diagram.visualization.planarization.rhd.GroupBuilder;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;

/**
 * Defines a way of merging leaf groups within the {@link GroupPhase} together to create a single compound group.
 * 
 * @author robmoffat
 *
 */
public interface GroupingStrategy extends GroupBuilder {

	public GroupResult group(GroupPhase gp);
			
}
