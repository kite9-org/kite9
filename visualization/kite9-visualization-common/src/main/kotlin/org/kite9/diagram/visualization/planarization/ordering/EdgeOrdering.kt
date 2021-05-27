package org.kite9.diagram.visualization.planarization.ordering

import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction

/**
 * Tracks directed and undirected edges arriving at a vertex or container, and the directions they arrive at.
 * @author robmoffat
 */
interface EdgeOrdering {

    /**
     * Returns either a [Direction], or the object MULTIPLE_DIRECTIONS
     */
    fun getEdgeDirections(): Any?

    fun size(): Int

    fun getIterator(
        clockwise: Boolean,
        startingAt: PlanarizationEdge,
        finish: PlanarizationEdge?,
        directedOnly: Boolean
    ): Iterator<PlanarizationEdge>

    /**
     * Tells the ordering that things have changed.
     */
    fun changed()

    /**
     * Allows you to figure out if a given directed edge can be inserted into the ordering
     * @param after Another DIRECTED edge in the ordering
     * @param d the direction you want to insert
     * @param clockwise whether you are inserting clockwise or anti-clockwise relative to after
     */
    fun canInsert(after: PlanarizationEdge, d: Direction?, clockwise: Boolean, log: Kite9Log): Boolean

    /**
     * Returns an unmodifiable list of leaving edges in clockwise order.
     */
    fun getEdgesAsList(): List<PlanarizationEdge>

    /**
     * Returns set of underlying diagram element leavers
     */
    fun getUnderlyingLeavers(): Set<DiagramElement>

    companion object {
		val MUTLIPLE_DIRECTIONS = Any()
    }
}