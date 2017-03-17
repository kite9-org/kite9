package org.kite9.diagram.visualization.compaction.position.optstep;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.algorithms.so.OptimisationStep;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.AbstractSegmentModifier;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.diagram.visualization.compaction.position.SegmentSlackOptimisation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;


/**
 * Handles separation of attr in the following scenarios:
 * 
 * <li>Where contexts / edges are invisible, but things either side need a distance set
 * @author robmoffat
 *
 */
public class EdgeSeparationOptimisationStep extends AbstractSegmentModifier implements OptimisationStep, Logable {

	private Kite9Log log = new Kite9Log(this);
	
	public EdgeSeparationOptimisationStep(CompleteDisplayer displayer) {
		super(displayer);
	}
	
	int dc;

	@Override
	public void optimise(Compaction c, SegmentSlackOptimisation xo,
			SegmentSlackOptimisation yo) {
		
		checkLengths(yo, Direction.DOWN); 
		checkLengths(xo, Direction.RIGHT); 
	}

	private void checkLengths(SegmentSlackOptimisation so, Direction d) {
		log.send("Length checking order:", so.getAllSlideables());
		
		for (Slideable s : so.getAllSlideables()) {
			boolean isVisible = checkVisibility((Segment) s.getUnderlying());
			
			
			if (isVisible) {
				Set<Slideable> forwardVisibles = new LinkedHashSet<>();
				s.withMinimumForwardConstraints(to -> collateVisibles(to, forwardVisibles));
				
				for (Slideable to : forwardVisibles) {
					double mdNew = getMinimumDistance(d==Direction.RIGHT, (Segment) s.getUnderlying(), 
							(Segment) to.getUnderlying());
								
					log.send(log.go() ? null : "Ensuring distance: "+s+"("+s.getPositionalOrder()+") to "+to+"("+to.getPositionalOrder()+") as "+mdNew+" as ends are both visible");
					so.ensureMinimumDistance(s, to, (int) mdNew);

				}
			}
		}

		log.send(log.go() ? null : "Completed edge separation "+d+" with "+so.getAllSlideables().size()+" slideables");

	}

	private void collateVisibles(Slideable v, Set<Slideable> forwardVisibles) {
		boolean isVisible2 = checkVisibility((Segment) v.getUnderlying());
		if (isVisible2) {
			forwardVisibles.add(v);
		} else {
			v.withMinimumForwardConstraints(to2 -> collateVisibles(to2, forwardVisibles));
		}
	}

	@Override
	public String getPrefix() {
		return "ESOS";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

	
	
}
