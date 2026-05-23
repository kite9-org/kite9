package org.kite9.diagram.common.algorithms.fg

interface FlowAlgorithm<X : FlowGraph> {

    /**
     * Maximises flow in the flow graph, and returns the cost of doing so
     */
    fun maximiseFlow(fg: X): Int
}