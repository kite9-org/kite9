package org.kite9.diagram.common.algorithms.fg

/**
 * A type of arc where positive or negative flow is permitted, within a boundary,
 * but any flow in either direction has a constant cost.
 * @author Rob Moffat
 */
open class AbsoluteArc(var cost: Int, protected var capacity: Int, from: Node, to: Node, label: String) :
    AbstractArc(from, to, label, 0) {

    open fun getFlowCost(): Int {
        return Math.abs(flow) * cost
    }

    override fun getIncrementalCost(flow: Int): Int {
        val origCost = Math.abs(this.flow) * cost
        val newCost = Math.abs(this.flow + flow) * cost
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