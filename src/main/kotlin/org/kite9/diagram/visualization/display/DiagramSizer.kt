package org.kite9.diagram.visualization.display

import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction

/**
 * Handles spacing between diagram components
 *
 * @author robmoffat
 */
interface DiagramSizer {
    /**
     * Returns the necessary distance between two diagram attr, possibly connected by an optional /along/.
     * If concave is set to false, then actually, we are not looking at a and b facing each other, and the distance should be entirely due to
     * the along part.
     */
    fun getMinimumDistanceBetween(
        a: DiagramElement,
        aSide: Direction,
        b: DiagramElement,
        bSide: Direction,
        direction: Direction,
        along: DiagramElement?,
        concave: Boolean
    ): Double

    /**
     * Determines whether you should draw a hop at the point connection a meets connection b
     */
    fun requiresHopForVisibility(a: Connection, b: Connection): Boolean
}