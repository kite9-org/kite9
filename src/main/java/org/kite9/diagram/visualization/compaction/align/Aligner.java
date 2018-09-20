package org.kite9.diagram.visualization.compaction.align;

import java.util.List;

import org.kite9.diagram.model.AlignedRectangular;
import org.kite9.diagram.visualization.compaction.Compaction;

public interface Aligner {
	
	/**
	 * Applies alignment to all rectangulars along a the same given axis.
	 * Each AlignedRectangular may have a different alignment, but they will have all passed 
	 * through willAlign with true.
	 */
	void alignRectangulars(List<AlignedRectangular> de, Compaction c, boolean horizontal);
	
	boolean willAlign(AlignedRectangular de, boolean horizontal);
}
