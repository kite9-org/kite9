package org.kite9.diagram.common.range

import org.kite9.diagram.common.range.IntegerRange

/**
 * An integer range, from and to.
 * Used for grid positioning in each axis.
 *
 * @author robmoffat
 */
data class BasicIntegerRange(override val from: Int, override val to: Int) : IntegerRange {

    override fun toString(): String {
        return if (from > to) {
            "*"
        } else {
            "$from $to"
        }
    }

}