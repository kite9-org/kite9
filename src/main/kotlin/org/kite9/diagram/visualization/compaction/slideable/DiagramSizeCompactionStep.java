package org.kite9.diagram.visualization.compaction.slideable;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.logging.Kite9Log;

/**
 * Ensures that the overall diagram width is minimal.
 * @author robmoffat
 *
 */
public class DiagramSizeCompactionStep extends AbstractCompactionStep {

	public DiagramSizeCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	Kite9Log log = new Kite9Log(this);
	
	public void compact(Compaction c, Embedding r, Compactor rc) {
		if (r.isTopEmbedding()) {
			setFor(c.getHorizontalSegmentSlackOptimisation());
			setFor(c.getVerticalSegmentSlackOptimisation());
		}
	}
	
	private void setFor(SegmentSlackOptimisation o) {
		OPair<Slideable<Segment>> diagramSlideables = o.getSlideablesFor(o.getTheDiagram());
		Slideable<Segment> highSide = diagramSlideables.getB();
		int min = highSide.getMinimumPosition();
		highSide.setMaximumPosition(min);
		log.send("Set Overall Diagram Size: "+highSide);
	}

	public String getPrefix() {
		return "DSCS";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}
