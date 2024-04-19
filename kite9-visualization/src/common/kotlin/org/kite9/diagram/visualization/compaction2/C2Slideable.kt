package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension

sealed class C2Slideable(so: C2SlackOptimisation, val dimension: Dimension) : Slideable(so) {

    val number: Int = nextNumber()

    var done = false

    companion object {

        var n: Int = 0

        fun nextNumber() : Int {
            n++
            return n
        }

    }

    fun routesTo(increasing: Boolean) : Map<C2Slideable, Int> {
        return if (increasing) {
            minimum.forward
                .map { (k, v) -> k.owner as C2Slideable to v }
                .toMap()
        } else {
            maximum.forward
                .map { (k, v) -> k.owner as C2Slideable to v }
                .toMap()
        }
    }
}