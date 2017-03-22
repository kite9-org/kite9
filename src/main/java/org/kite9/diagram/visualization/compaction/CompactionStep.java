package org.kite9.diagram.visualization.compaction;

import org.kite9.diagram.model.Rectangular;

/**
 * A step in which some part of the compaction process occurs.
 * 
 * 
 * @author robmoffat
 *
 */
public interface CompactionStep {

	public void compact(Compaction c, Rectangular r, Compactor rc);
	
}
