package org.kite9.diagram.visualization.compaction.slideable;

import java.util.LinkedHashSet;
import java.util.Set;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;


/**
 * Handles separation of attr in the following scenarios:
 * 
 * <li>Where contexts / edges are invisible, but things either side need a distance set
 * @author robmoffat
 *
 */
public class EdgeSeparationCompactionStep extends AbstractCompactionStep {
	
	public EdgeSeparationCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	int dc;

	@Override
	public void compact(Compaction c, Rectangular r, Compactor rc) {
		checkLengths(c.getYSlackOptimisation(), Direction.DOWN); 
		checkLengths(c.getXSlackOptimisation(), Direction.RIGHT); 
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
