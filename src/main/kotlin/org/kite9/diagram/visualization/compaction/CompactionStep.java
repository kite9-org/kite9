package org.kite9.diagram.visualization.compaction;

/**
 * A step in which some part of the compaction process occurs.
 * 
 * 
 * @author robmoffat
 *
 */
public interface CompactionStep {

	public void compact(Compaction c, Embedding e, Compactor rc);
	
}
