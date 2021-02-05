package org.kite9.diagram.common.algorithms.fg

import org.kite9.diagram.logging.LogicException

abstract class AbstractArc(override var from: Node, override var to: Node, val label: String, override var flow: Int = 0) : Arc {

    override fun getFlowFrom(n: Node): Int {
        return if (n === from) {
            flow
        } else if (n === to) {
            -flow
        } else {
            throw LogicException("Node not joined to arc")
        }
    }

    override fun pushFlow(flow: Int) {
        this.flow += flow
        from.pushFlow(-flow)
        to.pushFlow(flow)
    }

    override fun toString(): String {
        return label
    }

    override fun otherEnd(n: Node): Node {
        if (from === n) {
            return to
        }
        return if (to === n) from else throw IllegalArgumentException("Asking for end that's not set")
    }

    override fun hashCode(): Int {
        return label.hashCode()
    }

    override fun getID(): String {
        return label
    }

    init {
        from.arcs.add(this)
        to.arcs.add(this)
    }
}