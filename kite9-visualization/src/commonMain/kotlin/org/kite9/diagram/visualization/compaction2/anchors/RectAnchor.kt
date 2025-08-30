package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side

enum class Permeability { INCREASING, DECREASING, ALL, NONE }

/**
 * Anchor for part of a rectangular in a slideable, e.g. top, bottom etc.
 */
data class RectAnchor(override val e: Rectangular, override val s: Side, val permeability: Permeability) : Anchor<Side> {

    fun canCross(d: Direction) : Boolean {
        return when (d) {
            Direction.UP,
            Direction.LEFT
                -> (permeability == Permeability.DECREASING) || (permeability == Permeability.ALL)
            Direction.DOWN,
            Direction.RIGHT
                -> (permeability == Permeability.INCREASING) || (permeability == Permeability.ALL)
        }
    }
}