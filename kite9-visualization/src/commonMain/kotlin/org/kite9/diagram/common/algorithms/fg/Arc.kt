package org.kite9.diagram.common.algorithms.fg

import org.kite9.diagram.common.algorithms.det.Deterministic

interface Arc : Deterministic {

    fun getFlowFrom(n: Node): Int
    fun pushFlow(flow: Int)
    var from: Node
    var to: Node
    fun getIncrementalCost(flow: Int): Int
    var flow: Int
    fun otherEnd(n: Node): Node
    fun hasCapacity(reversed: Boolean): Boolean
}