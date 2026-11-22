package org.kite9.diagram.model.position

/**
 * Extra layout options for Containers
 *
 * @author robmoffat
 */
enum class Layout {
    HORIZONTAL, VERTICAL, LEFT, RIGHT, UP, DOWN, GRID;

    companion object {

        fun fromDirection(d: Direction?) : Layout? {
            return when(d) {
                Direction.UP -> Layout.UP
                Direction.DOWN -> DOWN
                Direction.LEFT -> Layout.LEFT
                Direction.RIGHT -> Layout.RIGHT
                null -> null
            }
        }

        fun toDirection(l: Layout?) : Direction? {
            return when(l) {
                Layout.UP -> Direction.UP
                Layout.DOWN -> Direction.DOWN
                Layout.LEFT -> Direction.LEFT
                Layout.RIGHT -> Direction.RIGHT
                else -> null
            }
        }

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


		fun isHorizontal(d: Layout?): Boolean {
            return if (d == null) true else when (d) {
                HORIZONTAL, LEFT, RIGHT -> true
                VERTICAL, UP, DOWN -> false
                else -> false
            }
        }
    }
}