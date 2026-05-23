package org.kite9.diagram.common.algorithms.fg

import org.kite9.diagram.common.algorithms.det.DetHashSet
import org.kite9.diagram.common.algorithms.fg.Node.ResidualStatus

/**
 * Basic implementation of the flow graph node
 * @author robmoffat
 */
open class SimpleNode(
    private val id: String,
    override var supply: Int,
    val representation: Any?) : Node {

    override var type: String = "UNDEFINED"
    override var flow = 0
    override var arcs: MutableSet<Arc> = DetHashSet()

    override fun toString(): String {
        return "$id($flow/$supply)"
    }

    override fun getID(): String {
        return id
    }

    override fun isLinkedTo(n: SimpleNode): Boolean {
        for (a in arcs) {
            if (a.otherEnd(this) === n) return true
        }
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun ensureEulersEquilibrium(): Boolean {
        return flow + supply == 0
    }

    override fun pushFlow(flow: Int) {
        this.flow += flow
    }

    override fun getResidualStatus(): ResidualStatus {
        val amt = supply + flow
        return if (amt < 0) {
            ResidualStatus.SINK
        } else if (amt > 0) {
            ResidualStatus.SOURCE
        } else {
            ResidualStatus.SATISFIED
        }
    }
}