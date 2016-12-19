package org.kite9.diagram.visualization.compaction.position;

import java.util.List;

import org.kite9.diagram.common.algorithms.so.OptimisationStep;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.framework.logging.LogicException;


/**
 * This segment positioner has multiple, pluggable components that can optimise the
 * positioning of the segments.  It uses the {@link SegmentSlackOptimisation} as the model 
 * for all of the optimisation steps.
 *
 * @author robmoffat
 *
 */
public class OptimisablePositionerCompactionStep extends AbstractSegmentPositioner {
	
	public OptimisablePositionerCompactionStep(OptimisationStep[] steps) {
		this.steps = steps;
	}
	
	private OptimisationStep[] steps;
	
	public void compactDiagram(Compaction c) {

		// holds the state of the optimisation
		SegmentSlackOptimisation xso = new SegmentSlackOptimisation(c.getVerticalSegments(), Direction.RIGHT);
		SegmentSlackOptimisation yso = new SegmentSlackOptimisation(c.getHorizontalSegments(), Direction.DOWN);
		
		for (OptimisationStep step : steps) {
			log.send("Running Optimisation Step: "+step.getClass());
			try {
				step.optimise(c, xso, yso);
			} finally {
				log.send("X Optimisation", xso.getCanonicalOrder());
				log.send("Y Optimisation", yso.getCanonicalOrder());
			}
		}
		
		setSegmentPostions(xso);
		setSegmentPostions(yso);
		
		if (!setAndCheckDartPositions(c.getVerticalSegments())) {
			throw new LogicException("vertical segments contain a cycle and cannot be positioned");
		}
		if (!setAndCheckDartPositions(c.getHorizontalSegments())) {
			throw new LogicException("horizontal segments contain a cycle and cannot be positioned");
		}
	}



	
	private void setSegmentPostions(SegmentSlackOptimisation opt) {
		List<Slideable> slideables = opt.getCanonicalOrder();
		for (Slideable s : slideables) {
			double pos = s.getMinimumPosition();
			Segment seg = (Segment) s.getUnderlying();
			seg.setPosition(pos);
			log.send(log.go() ? null : "Position "+s.getMinimumPosition()+" for "+seg);
		}
	}

}
