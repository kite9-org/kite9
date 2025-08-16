/**
 *
 */
package org.kite9.diagram.model.position

import org.kite9.diagram.model.position.HPos

enum class HPos {
    LEFT, RIGHT;

    val direction: Direction
        get() = if (ordinal == 0) {
            Direction.LEFT
        } else {
            Direction.RIGHT
        }

    companion object {
        fun getFromDirection(d: Direction?): HPos? {
            return when (d) {
                Direction.LEFT -> LEFT
                Direction.RIGHT -> RIGHT
                else -> null
            }
        }
    }
}