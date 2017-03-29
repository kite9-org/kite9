package org.kite9.diagram.visualization.compaction.slideable;

import java.util.Collection;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;

public class VertexPositioningCompactionStep extends AbstractCompactionStep {

	public VertexPositioningCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}


	@Override
	public void compact(Compaction c, Rectangular r, Compactor rc) {
		if (r instanceof Diagram) {
			setSegmentPostions(c.getXSlackOptimisation());
			setSegmentPostions(c.getXSlackOptimisation());
		}
	}


	private void setSegmentPostions(SegmentSlackOptimisation opt) {
		opt.updatePositionalOrdering();
		Collection<Slideable> slideables = opt.getPositionalOrder();
		for (Slideable s : slideables) {
			double pos = s.getMinimumPosition();
			Segment seg = (Segment) s.getUnderlying();
			seg.setPosition(pos);
			log.send(log.go() ? null : "Position " + s.getMinimumPosition() + " for " + seg);
		}
	}


	@Override
	public String getPrefix() {
		return "VERT";
	}


	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

}
