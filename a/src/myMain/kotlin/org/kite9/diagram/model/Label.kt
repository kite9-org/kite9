package org.kite9.diagram.model

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.End

/**
 * DiagramElement to contain a label for an edge, container or diagram.
 * Labels take up space on the diagram, so they have to be processed in the *orthogonalization* phase.
 * however they don't have connections so they are excluded from the Planarization phase.
 */
interface Label : Rectangular {

    fun isConnectionLabel(): Boolean

    /**
     * If this is a connection label, returns the end of the connection that it is for.
     */
    fun getEnd(): End?

    fun getLabelPlacement(): Direction?

    companion object {
        /**
         * Same as above, but defaults to down
         */
        fun getLabelPlacementDefaulted(l: Label) = l.getLabelPlacement() ?: Direction.DOWN;

        /**
         * @param d Direction of link
         * @param default usual direction of link, (down or right)
         */
        fun connectionLabelPlacementDirection(l: Label, d: Direction, default: Direction): Direction {
            return if (d === Direction.UP || d === Direction.DOWN) {
                when (l.getLabelPlacement()) {
                    Direction.LEFT -> Direction.RIGHT
                    else -> default
                }
            } else {
                when (l.getLabelPlacement()) {
                    Direction.UP -> Direction.DOWN
                    else -> default
                }
            }
        }

        /**
         * Works out whether a given edge will host a label, given the placement of the
         * label.
         */
        fun containerLabelPlacement(l: Label, d: Direction, default: Direction): Boolean {
            return when (l.getLabelPlacement()) {
                Direction.UP -> d === Direction.UP
                Direction.DOWN -> d === Direction.DOWN
                Direction.LEFT -> d === Direction.LEFT
                Direction.RIGHT -> d === Direction.RIGHT
                else -> d === default
            }
        }
    }
}

