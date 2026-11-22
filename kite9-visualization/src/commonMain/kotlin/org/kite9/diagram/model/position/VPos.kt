/**
 *
 */
package org.kite9.diagram.model.position

import org.kite9.diagram.model.position.VPos

enum class VPos {
    UP, DOWN;

    val direction: Direction
        get() = if (ordinal == 0) {
            Direction.UP
        } else {
            Direction.DOWN
        }

    companion object {
        fun getFromDirection(d: Direction?): VPos? {
            return when (d) {
                Direction.UP -> UP
                Direction.DOWN -> DOWN
                else -> null
            }
        }
    }
}