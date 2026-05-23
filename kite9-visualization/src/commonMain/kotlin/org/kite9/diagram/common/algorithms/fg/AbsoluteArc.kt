package org.kite9.diagram.common.algorithms.fg

import kotlin.math.abs

/**
 * A type of arc where positive or negative flow is permitted, within a boundary,
 * but any flow in either direction has a constant cost.
 * @author Rob Moffat
 */
open class AbsoluteArc(val cost: Int, protected val capacity: Int, from: Node, to: Node, label: String) :
    AbstractArc(from, to, label, 0) {

    open fun getFlowCost(): Int {
        return abs(flow) * cost
    }

    override fun getIncrementalCost(flow: Int): Int {
        val origCost = abs(this.flow) * cost
        val newCost = abs(this.flow + flow) * cost
        return newCost - origCost
    }

    override fun hasCapacity(reversed: Boolean): Boolean {
        return if (!reversed) {
            flow < capacity
        } else {
            flow > -capacity
        }
    }
}