package org.kite9.diagram.model.style

import org.kite9.diagram.common.range.IntegerRange

data class GridContainerPosition(val x: IntegerRange, val y: IntegerRange) : ContainerPosition {

    override fun toString(): String {
        return "[" + x.from + "," + x.to + "," + y.from + "," + y.to + "]"
    }
}