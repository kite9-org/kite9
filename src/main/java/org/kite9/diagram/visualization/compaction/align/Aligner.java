package org.kite9.diagram.visualization.compaction.align;

import java.util.Set;

import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.Compaction;

public interface Aligner {
	
	/**
	 * Applies alignment to all rectangulars along a the same given axis.
	 * Each AlignedRectangular may have a different alignment, but they will have all passed 
	 * through willAlign with true.
	 */
	void alignFor(Container co, Set<Rectangular> de, Compaction c, boolean horizontal);
	
	boolean willAlign(Rectangular de, boolean horizontal);
}
