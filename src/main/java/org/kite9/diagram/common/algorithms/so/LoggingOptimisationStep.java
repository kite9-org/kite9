package org.kite9.diagram.common.algorithms.so;

import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.position.SegmentSlackOptimisation;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

public class LoggingOptimisationStep implements OptimisationStep, Logable {
	
	private Kite9Log log = new Kite9Log(this);

	@Override
	public void optimise(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo) {
		log.send(log.go() ? null : "Minimisation Steps: X: "+xo.maxCount+" Y: "+yo.maxCount+" T: "+ (xo.maxCount + yo.maxCount));
		log.send(log.go() ? null : "Push Steps: X: "+xo.pushCount+ " Y: "+yo.pushCount+ "T: "+(xo.pushCount + yo.pushCount));
	}

	@Override
	public String getPrefix() {
		return "LOPS";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

}
