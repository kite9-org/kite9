package org.kite9.diagram.visualization.compaction

import org.kite9.diagram.model.position.Direction

enum class Side {
    START, END, NEITHER;

    fun isEntering(d: Direction) : Boolean {
        return when (this) {
            START -> d == Direction.RIGHT || d == Direction.DOWN
            END -> d == Direction.LEFT || d == Direction.UP
            NEITHER -> false
        }
    }
}