package org.kite9.diagram.visualization.compaction.slideable;

import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

public class LoggingOptimisationStep extends AbstractCompactionStep {

	public LoggingOptimisationStep(CompleteDisplayer cd) {
		super(cd);
	}

	@Override
	public String getPrefix() {
		return "LOPS";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

	@Override
	public void compact(Compaction c, Embedding r, Compactor rc) {
		log.send("Embedding: "+r);
		optimise(c, c.getHorizontalSegmentSlackOptimisation(), c.getVerticalSegmentSlackOptimisation());
	}

	public void optimise(Compaction c, SegmentSlackOptimisation horizontalSegments, SegmentSlackOptimisation verticalSegments) {
		log.send(log.go() ? null : "Minimisation Steps: \n  HorizontalSegments: "+horizontalSegments.getSize()+" \n  Vertical Segments: "+verticalSegments.getSize()+" T: "+ (horizontalSegments.getSize() + verticalSegments.getSize()));
		log.send(log.go() ? null : "Push Steps: \n  Horizontal Segments: "+horizontalSegments.getPushCount()+ " \n  Vertical Segments: "+verticalSegments.getPushCount()+ "T: "+(horizontalSegments.getPushCount() + verticalSegments.getPushCount()));
		log.send("Horizontal Segments:", horizontalSegments.getAllSlideables());
		log.send("Vertical Segments:", verticalSegments.getAllSlideables());
	}
}
