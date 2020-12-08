package org.kite9.diagram.model.position

import org.kite9.diagram.model.position.Turn

enum class Direction {
    UP, LEFT, DOWN, RIGHT;

    fun getDirectionChange(to: Direction): Turn {
        val change = (to.ordinal - ordinal + 4) % 4
        return Turn.values()[change]
    }

    companion object {
        @JvmStatic
		fun rotateAntiClockwise(d: Direction): Direction {
            var ord = d.ordinal
            ord = ord + 1
            ord = ord % 4
            return values()[ord]
        }

        @JvmStatic
		fun rotateClockwise(d: Direction): Direction {
            var ord = d.ordinal
            ord = ord + 3
            ord = ord % 4
            return values()[ord]
        }

        @JvmStatic
		fun reverse(d: Direction?): Direction? {
            if (d == null) return null
            var ord = d.ordinal
            ord = ord + 2
            ord = ord % 4
            return values()[ord]
        }

        fun getDirection(s: String?): Direction? {
            return if (s == null || s.trim { it <= ' ' }.length == 0) {
                null
            } else {
                valueOf(s)
            }
        }

        @JvmStatic
		fun isHorizontal(d: Direction): Boolean {
            return d == LEFT || d == RIGHT
        }

        @JvmStatic
		fun isVertical(d: Direction): Boolean {
            return d == UP || d == DOWN
        }
    }
}