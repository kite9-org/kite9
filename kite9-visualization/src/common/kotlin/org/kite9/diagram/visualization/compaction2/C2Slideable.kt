package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension

open class C2Slideable(so: SlackOptimisation, val dimension: Dimension) : Slideable(so) {

    val number: Int = nextNumber()

    companion object {

        var n: Int = 0

        fun nextNumber() : Int {
            n++
            return n
        }

    }

}