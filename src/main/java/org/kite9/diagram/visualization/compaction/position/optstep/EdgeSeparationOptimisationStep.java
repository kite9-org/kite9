package org.kite9.diagram.visualization.compaction.position.optstep;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.algorithms.so.OptimisationStep;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
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
		
		xo.updatePositionalOrdering();
		yo.updatePositionalOrdering();

		int capacity = yo.getCanonicalOrder().size()*2;
		checkLengths(yo, Direction.DOWN, new HashMap<Slideable,  Set<Slideable>>(capacity)); 
		capacity = xo.getCanonicalOrder().size()*2;
		checkLengths(xo, Direction.RIGHT, new HashMap<Slideable,  Set<Slideable>>(capacity)); 
		
	}

	private void checkLengths(SegmentSlackOptimisation so, Direction d, Map<Slideable, Set<Slideable>> toFromMap) {
		int count = 0;
		for (Slideable s : so.getPositionalOrder()) {
			boolean isVisible = checkVisibility((Segment) s.getUnderlying());
			Set<Slideable> from = toFromMap.get(s);

			if (isVisible) {
				if (from != null) {
					for (Slideable f : from) {
						double mdNew = getMinimumDistance(d==Direction.RIGHT, (Segment) f.getUnderlying(), 
								(Segment) s.getUnderlying());
									
						log.send(log.go() ? null : "Ensuring distance: "+f+"("+f.getPositionalOrder()+") to "+s+"("+s.getPositionalOrder()+") as "+mdNew+" as ends are both visible");
						so.ensureMinimumDistance(f, s, (int) mdNew, true);
						count ++;
					}
				} else {
					from = new HashSet<Slideable>(2);
				}
				
				from.clear();
				from.add(s);
			}
			
			
			Set<Slideable> pushing = new HashSet<Slideable>();
			
			// work out what this slideable pushes on
			for (Vertex v : ((Segment) s.getUnderlying()).getVerticesInSegment()) {
				for (Edge e : v.getEdges()) {
					if ((e instanceof Dart) && (e.getDrawDirectionFrom(v) == d)) {
						Slideable next = so.getVertexToSlidableMap().get(e.otherEnd(v));
						if (next != s) {
							pushing.add(next);
						}
					}
				}
			}

			// make sure we track any forward pushes
			for (Slideable to : pushing) {
				Set<Slideable> fromMap = toFromMap.get(to);
				if (fromMap == null) {
					fromMap = new HashSet<Slideable>(10);
					toFromMap.put(to, fromMap);
				}
				fromMap.addAll(from);
			}
		}

		log.send(log.go() ? null : "Completed edge separation "+d+" with "+so.getCanonicalOrder().size()+" slideables and "+count+" checks");

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
