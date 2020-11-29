package org.kite9.diagram.common.algorithms.fg;

public interface FlowAlgorithm<X extends FlowGraph> {

	/**
	 * Maximises flow in the flow graph, and returns the cost of doing so
	 */
	public int maximiseFlow(X fg);
}
