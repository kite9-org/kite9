package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Positioned
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side

enum class Permeability { INCREASING, DECREASING, ALL, NONE }

/**
 * Anchor for part of a rectangular in a slideable, e.g. top, bottom etc.
 */
data class RectAnchor(override val e: Positioned, override val s: Side, val permeability: Permeability) : Anchor<Side> {

    /**
     * Takes permeability into account when deciding whether we can cross over this element
     * when routing.
     *
     * Sometimes we have to ignore permeability - if we are crossing to something inside e,
     * we just have to do it, hence the passport: elements that we are definitely allowed to cross.
     */
    fun canCross(d: Direction, passport: Set<DiagramElement>) : Boolean {
        if (passport.contains(e)) {
            return true
        }

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