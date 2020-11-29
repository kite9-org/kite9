package org.kite9.diagram.common.algorithms.fg;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.kite9.diagram.common.algorithms.ssp.AbstractSSP;
import org.kite9.diagram.common.algorithms.ssp.State;

/**
 * Optimised flow graph, for when there are multiple sources and sinks.
 * This saves a bunch of time at the start of the SSP by not adding all the sources to the priority queue.
 * 
 * @author robmoffat
 *
 */
public class RapidFlowGraphSSP<X extends FlowGraph> extends FlowGraphSPP<X> {

	public RapidFlowGraphSSP() {
		super();
	}

	@Override
	protected void createInitialPaths(State<Path> pq) {
		((OptimisedPathState)pq).setDestinations(destination);
	}

	
	@Override
	protected State<Path> createState() {
		return new OptimisedPathState(this);
	}
	
	public class OptimisedPathState extends State<Path> {

		public OptimisedPathState(AbstractSSP<Path> in) {
			super(in);
		}
		
		private Deque<Node> destinations;

		/**
		 * Call this once to set up the destinations that the 
		 * @param destination 
		 */
		public void setDestinations(List<Node> destination) {
			this.destinations = new ArrayDeque<Node>(destination);
		}

		public static final int THRESHOLD = 3;
		
		@Override
		public Path remove() {
			Path out = pq.peek();
			if (((out==null) || (out.getCost() > THRESHOLD)) &&  (destinations.size() > 0)) {
				out = new Path(destinations.pop());
				return out;
			}
			
			
			return super.remove();
		}
		
		
	}

}
