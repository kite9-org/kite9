package org.kite9.diagram.common.algorithms.fg

import org.kite9.diagram.common.algorithms.det.Deterministic

/**
 * A node is a join point for an arc.
 * @author robmoffat
 */
interface Node : Deterministic {

    enum class ResidualStatus {
        SATISFIED, SOURCE, SINK
    }

    fun ensureEulersEquilibrium(): Boolean

    var type: String
    var arcs: MutableSet<Arc>
    fun isLinkedTo(n: SimpleNode): Boolean
    var supply: Int
    var flow: Int
    fun pushFlow(flow: Int)
    fun getResidualStatus(): ResidualStatus
}