package org.kite9.diagram.common.algorithms.so;

import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.position.SegmentSlackOptimisation;

/**
 * A step in the process of setting the positions.
 * 
 * @author robmoffat
 *
 */
public interface OptimisationStep {

	public void optimise(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo);
}
