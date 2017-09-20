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
		log.send(log.go() ? null : "Minimisation Steps: \n  HorizontalSegments: "+horizontalSegments.maxCount+" \n  Vertical Segments: "+verticalSegments.maxCount+" T: "+ (horizontalSegments.maxCount + verticalSegments.maxCount));
		log.send(log.go() ? null : "Push Steps: \n  Horizontal Segments: "+horizontalSegments.pushCount+ " \n  Vertical Segments: "+verticalSegments.pushCount+ "T: "+(horizontalSegments.pushCount + verticalSegments.pushCount));
		log.send("Horizontal Segments:", horizontalSegments.getAllSlideables());
		log.send("Vertical Segments:", verticalSegments.getAllSlideables());
	}
}
