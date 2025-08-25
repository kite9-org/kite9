package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side

/**
 * This is an anchor which has information for the router to tell it if a
 * route perpendicular to a slideable with this anchor on it can be crossed
 * or not.
 */

enum class Permeability { INCREASING, DECREASING, ALL, NONE }

interface PermeableAnchor : Anchor<Side> {

    override val e: Rectangular

    val permeability: Permeability

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