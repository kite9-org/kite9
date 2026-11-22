package org.kite9.diagram.model.style

import org.kite9.diagram.common.range.IntegerRange

/**
 * Represents the space a cell can take up (in a single
 * dimension), within a rectangular area divided into a grid.
 */
data class GridRectangularPosition(val r: IntegerRange) : RectangularPosition {

    fun isSet(): Boolean {
        return !IntegerRange.notSet(r)
    }

    override fun toString(): String {
        return if (!isSet()) {
            "null"
        } else {
            "[" + r.from + "," + r.to + "]"
        }
    }

    fun getFrom() : Int {
        return r.from
    }

    fun getTo() : Int {
        return r.to
    }
}