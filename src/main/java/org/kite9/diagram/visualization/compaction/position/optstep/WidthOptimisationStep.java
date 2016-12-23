package org.kite9.diagram.visualization.compaction.position.optstep;

import org.kite9.diagram.common.algorithms.so.OptimisationStep;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.position.SegmentSlackOptimisation;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

/**
 * Ensures that the overall diagram width is minimal.
 * @author robmoffat
 *
 */
public class WidthOptimisationStep implements OptimisationStep, Logable {

	Kite9Log log = new Kite9Log(this);
	
	public void optimise(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo) {
		setFor(xo);
		setFor(yo);
	}

	private void setFor(SegmentSlackOptimisation o) {
		OPair<Slideable> diagramSlideables = o.getSlideablesFor(o.getTheDiagram());
		Slideable highSide = diagramSlideables.getB();
		int min = highSide.getMinimumPosition();
		highSide.setMaximumPosition(min);
	}

	public String getPrefix() {
		return "WOS ";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}
