package org.kite9.diagram.model.style

import org.kite9.diagram.common.range.IntegerRange

data class GridContainerPosition(val r: IntegerRange) : ContainerPosition {

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