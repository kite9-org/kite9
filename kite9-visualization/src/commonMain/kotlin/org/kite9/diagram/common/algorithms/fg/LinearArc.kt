package org.kite9.diagram.common.algorithms.fg

import org.kite9.diagram.common.algorithms.fg.AbstractArc

/**
 * A linear arc has a constant cost for each item of flow pushed into it.
 * It has upper and lower bounds on the amount of flow that can travel through it.
 * @author Rob Moffat
 */
class LinearArc(
    protected var cost: Int,
    protected var capacity: Int,
    protected var lowerBound: Int,
    from: Node,
    to: Node,
    label: String
) : AbstractArc(from, to, label, 0) {

    override fun getIncrementalCost(flow: Int): Int {
        return flow * cost
    }

    override fun hasCapacity(reversed: Boolean): Boolean {
        return if (!reversed) {
            flow < capacity
        } else {
            flow > lowerBound
        }
    }

}