package org.kite9.diagram.model.position

/**
 * Extra layout options for Containers
 *
 * @author robmoffat
 */
enum class Layout {
    HORIZONTAL, VERTICAL, LEFT, RIGHT, UP, DOWN, GRID;

    companion object {
        @JvmStatic
		fun reverse(d: Layout?): Layout? {
            return if (d == null) null else when (d) {
                HORIZONTAL -> HORIZONTAL
                VERTICAL -> VERTICAL
                LEFT -> RIGHT
                RIGHT -> LEFT
                UP -> DOWN
                else -> UP
            }
        }

        @JvmStatic
		fun rotateClockwise(d: Layout?): Layout? {
            return if (d == null) null else when (d) {
                HORIZONTAL -> VERTICAL
                VERTICAL -> HORIZONTAL
                LEFT -> UP
                RIGHT -> DOWN
                UP -> RIGHT
                else -> LEFT
            }
        }

        @JvmStatic
		fun rotateAntiClockwise(d: Layout?): Layout? {
            return if (d == null) null else when (d) {
                HORIZONTAL -> VERTICAL
                VERTICAL -> HORIZONTAL
                LEFT -> DOWN
                RIGHT -> UP
                UP -> LEFT
                else -> RIGHT
            }
        }

        @JvmStatic
		fun isHorizontal(d: Layout?): Boolean {
            return if (d == null) true else when (d) {
                HORIZONTAL, LEFT, RIGHT -> true
                VERTICAL, UP, DOWN -> false
                else -> false
            }
        }
    }
}