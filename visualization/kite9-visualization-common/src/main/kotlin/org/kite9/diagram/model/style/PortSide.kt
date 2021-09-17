package org.kite9.diagram.model.style

import org.kite9.diagram.model.position.Direction

enum class PortSide {

    TOP, LEFT, RIGHT, BOTTOM;

    fun getDirection() : Direction {
        return when (this) {
            TOP -> Direction.UP
            LEFT -> Direction.LEFT
            RIGHT -> Direction.RIGHT
            BOTTOM -> Direction.DOWN
        }
    }
}