package org.kite9.diagram.model.style

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.isHorizontal
import org.kite9.diagram.model.position.Direction.Companion.isVertical
import org.kite9.diagram.model.style.LabelPlacement

enum class LabelPlacement {

    TOP, LEFT, BOTTOM, RIGHT, TOP_LEFT, BOTTOM_LEFT, TOP_RIGHT, BOTTOM_RIGHT;

    fun containerLabelPlacement(d: Direction): Boolean {
        return when (this) {
            TOP, TOP_LEFT, TOP_RIGHT -> d === Direction.UP
            LEFT -> d === Direction.LEFT
            RIGHT -> d === Direction.RIGHT
            BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM -> d === Direction.DOWN
            else -> d === Direction.DOWN
        }
    }

    fun sameAxis(d: Direction?): Boolean {
        return when (this) {
            TOP, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM -> isHorizontal(
                d!!
            )
            LEFT, RIGHT -> isVertical(d!!)
            else -> isHorizontal(d!!)
        }
    }

    fun connectionLabelPlacementDirection(d: Direction): Direction {
        return if (d === Direction.UP || d === Direction.DOWN) {
            when (this) {
                LEFT, TOP_LEFT, BOTTOM_LEFT -> Direction.RIGHT
                TOP, BOTTOM, BOTTOM_RIGHT, TOP_RIGHT, RIGHT -> Direction.LEFT
                else -> Direction.LEFT
            }
        } else {
            when (this) {
                RIGHT, BOTTOM, LEFT, BOTTOM_LEFT -> Direction.UP
                BOTTOM_RIGHT, TOP, TOP_LEFT, TOP_RIGHT -> Direction.DOWN
                else -> Direction.UP
            }
        }
    }

    // TODO Auto-generated method stub
    val isVertical: Any?
        get() =// TODO Auto-generated method stub
            null
}