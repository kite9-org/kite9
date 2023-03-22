package org.kite9.diagram.model.style

import org.kite9.diagram.common.range.IntegerRange

data class GridContainerPosition(val x: IntegerRange, val y: IntegerRange) : ContainerPosition {

    fun isSet(): Boolean {
        return !IntegerRange.notSet(x) && !IntegerRange.notSet(y)
    }

    override fun toString(): String {
        return if (!isSet()) {
            "null"
        } else {
            "[" + x.from + "," + x.to + "," + y.from + "," + y.to + "]"
        }
    }
}