package org.kite9.diagram.visualization.compaction.position.optstep;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.kite9.diagram.common.algorithms.so.OptimisationStep;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.diagram.visualization.compaction.position.SegmentSlackOptimisation;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.planarization.mapping.ConnectionEdge;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

/**
 * Old, unused version of the slack centering optimisation step.
 * By the time this OptimisationStep occurs, we have already minimized the size of the glyphs, so this is
 * about reducing the length of diagram sections with links in (i.e. edges).  
 * 
 * Basically we do this by ordering positionables according to how many edges are active over them, and 
 * reducing slack on the positionables with the most, first.
 * 
 * @author robmoffat
 *
 */
public class LinkLengthReductionOptimisationStep implements OptimisationStep, Logable {
	
	Kite9Log log = new Kite9Log(this);

	public void optimise(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo) {
		log.send("Optimising Connection Lengths - horiz");
		centerContentRule(xo);
		log.send("Optimising Connection Lengths - vertical");
		centerContentRule(yo);
	}
	
	static class SlideableLinkCount implements Comparable<SlideableLinkCount>{

		public SlideableLinkCount(int cost, Slideable from, Slideable to) {
			super();
			this.cost = cost;
			this.from = from;
			this.to = to;
			this.distance = to.getPositionalOrder() - from.getPositionalOrder(); 
		}
		
		int cost;
		Slideable from;
		Slideable to;
		int distance;
		
		@Override
		public int compareTo(SlideableLinkCount arg0) {
			return new Integer(arg0.cost).compareTo(this.cost);
		}

		@Override
		public String toString() {
			return cost+" from: "+from+" to: "+to;
		}
		
		
	}

	private void centerContentRule(SegmentSlackOptimisation so) {
		PriorityQueue<SlideableLinkCount> queue = new PriorityQueue<SlideableLinkCount>(so.getCanonicalOrder().size());
		for (Slideable s : so.getCanonicalOrder()) {
			Map<Slideable, Integer> costs = getCostsToNextSlideables((Segment) s.getUnderlying(), so.getDirection() == Direction.RIGHT, so.getVertexToSlidableMap());
			
			for (Map.Entry<Slideable, Integer> sm : costs.entrySet()) {
				queue.add(new SlideableLinkCount(sm.getValue(), s, sm.getKey()));
			}
		}
		
		for (SlideableLinkCount slc : queue) {
			Slideable from = slc.from;
			Slideable to = slc.to;
			Integer minDist = from.minimumDistanceTo(to);
			so.ensureMaximumDistance(from, to, minDist, true);
		}
		
		log.send("Queue of linked segments", queue);
	}

	public Map<Slideable, Integer> getCostsToNextSlideables(Segment s, boolean horiz, Map<Vertex, Slideable> vertexToSlidableMap) {
		Map<Slideable, Integer> out = new HashMap<Slideable, Integer>();
		for (Vertex v : s.getVerticesInSegment()) {
			for (Edge e : v.getEdges()) {
				if (e instanceof Dart) { 
					Object und = ((Dart) e).getUnderlying();
					if (und instanceof ConnectionEdge) {
						Vertex otherEnd = e.otherEnd(v);
						Slideable otherSlideable = vertexToSlidableMap.get(otherEnd);
						Integer connectionAmount = out.get(otherSlideable);
						connectionAmount = connectionAmount ==  null ? 0 : connectionAmount;
						
						if (!horiz) {
							if (e.getDrawDirectionFrom(v)==Direction.DOWN) {
								connectionAmount += ((ConnectionEdge)und).getLengthCost();
							}
						} else {
							if (e.getDrawDirectionFrom(v)==Direction.RIGHT) {
								connectionAmount +=  ((ConnectionEdge)und).getLengthCost();
							}
						}
						
						if (connectionAmount > 0) {
							out.put(otherSlideable, connectionAmount);
						}
					}
				}
			}
		}
		
		return out;
	}

	@Override
	public String getPrefix() {
		return "CLOS";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
}
