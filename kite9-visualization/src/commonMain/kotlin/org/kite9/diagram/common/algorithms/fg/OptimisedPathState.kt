package org.kite9.diagram.common.algorithms.fg

import org.kite9.diagram.common.algorithms.ssp.AbstractSSP
import org.kite9.diagram.common.algorithms.ssp.State

class OptimisedPathState(o: AbstractSSP<Path>) : State<Path>(o) {

    private var destinations: ArrayDeque<Node>? = null

    /**
     * Call this once to set up the destinations that the
     * @param destination
     */
    fun setDestinations(destination: List<Node>) {
        destinations = ArrayDeque(destination)
    }

    override fun remove(): Path {
        var out = pq.peek()

        if ((out == null || out.getCost() > RapidFlowGraphSSP.THRESHOLD) && destinations!!.size > 0) {
            out = Path(destinations!!.removeFirst())
            return out
        }

        return super.remove()
    }

}