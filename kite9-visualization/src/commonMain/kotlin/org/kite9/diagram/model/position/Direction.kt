package org.kite9.diagram.model.position

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.position.Turn

enum class Direction {
    UP, LEFT, DOWN, RIGHT;

    fun getDirectionChange(to: Direction): Turn {
        val change = (to.ordinal - ordinal + 4) % 4
        return Turn.values()[change]
    }

    companion object {

		fun rotateAntiClockwise(d: Direction): Direction {
            var ord = d.ordinal
            ord = ord + 1
            ord = ord % 4
            return values()[ord]
        }


		fun rotateClockwise(d: Direction): Direction {
            var ord = d.ordinal
            ord = ord + 3
            ord = ord % 4
            return values()[ord]
        }


		fun reverse(d: Direction?): Direction? {
            if (d == null) return null
            var ord = d.ordinal
            ord = ord + 2
            ord = ord % 4
            return values()[ord]
        }

		fun isHorizontal(d: Direction): Boolean {
            return d == LEFT || d == RIGHT
        }


		fun isVertical(d: Direction): Boolean {
            return d == UP || d == DOWN
        }


        fun getDirection(d: Dimension, increasing: Boolean) : Direction {
            return if (d == Dimension.V) {
                if (increasing) {
                    RIGHT
                } else {
                    LEFT
                }
            } else {
                if (increasing) {
                    DOWN
                } else {
                    UP
                }
            }
        }
    }
}