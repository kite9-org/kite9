package org.kite9.diagram.visualization.compaction.slideable;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.framework.logging.Kite9Log;

/**
 * Ensures that the overall diagram width is minimal.
 * @author robmoffat
 *
 */
public class WidthCompactionStep extends AbstractCompactionStep {

	public WidthCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	Kite9Log log = new Kite9Log(this);
	
	public void compact(Compaction c, Rectangular r, Compactor rc) {
		if (r instanceof Diagram) {
			setFor(c.getXSlackOptimisation());
			setFor(c.getYSlackOptimisation());
		}
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
