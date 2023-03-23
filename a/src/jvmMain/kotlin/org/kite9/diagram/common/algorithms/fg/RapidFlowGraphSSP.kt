package org.kite9.diagram.common.algorithms.fg

import org.kite9.diagram.common.algorithms.ssp.State

/**
 * Optimised flow graph, for when there are multiple sources and sinks.
 * This saves a bunch of time at the start of the SSP by not adding all the sources to the priority queue.
 *
 * @author robmoffat
 */
class RapidFlowGraphSSP<X : FlowGraph> : FlowGraphSPP<X>() {

    override fun createInitialPaths(pq: State<Path>) {
        (pq as OptimisedPathState).setDestinations(getDestination())
    }

    override fun createState(): State<Path> {
        return OptimisedPathState(this)
    }

    companion object {
        const val THRESHOLD = 3
    }
}