package org.kite9.diagram.visualization.compaction.slideable;

import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
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
	public void compact(Compaction c, Rectangular r, Compactor rc) {
		optimise(c, c.getHorizontalSegmentSlackOptimisation(), c.getVerticalSegmentSlackOptimisation());
	}

	public void optimise(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo) {
		log.send(log.go() ? null : "Minimisation Steps: X: "+xo.maxCount+" Y: "+yo.maxCount+" T: "+ (xo.maxCount + yo.maxCount));
		log.send(log.go() ? null : "Push Steps: X: "+xo.pushCount+ " Y: "+yo.pushCount+ "T: "+(xo.pushCount + yo.pushCount));
		log.send("Xo:", xo.getAllSlideables());
		log.send("Yo:", yo.getAllSlideables());
	}
}
