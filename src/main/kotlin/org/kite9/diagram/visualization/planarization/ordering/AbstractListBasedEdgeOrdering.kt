package org.kite9.diagram.visualization.planarization.ordering

import org.kite9.diagram.common.elements.edge.PlanarizationEdge

/**
 * Implements a good deal of the functionality of the EdgeOrdering using the underlying list.
 *
 * @author robmoffat
 */
abstract class AbstractListBasedEdgeOrdering : AbstractEdgeOrdering() {

    override fun size(): Int {
        return getEdgesAsList().size
    }

    override fun getIterator(
        clockwise: Boolean,
        startingAt: PlanarizationEdge,
        finish: PlanarizationEdge?,
        directedOnly: Boolean
    ): Iterator<PlanarizationEdge> {
        return object : AbstractEdgeIterator(clockwise, startingAt, finish, directedOnly) {
            var underlying = getEdgesAsList()
            var i = underlying.indexOf(startingAt)
            override fun getNext(): PlanarizationEdge? {
                i = (i + (if (clockwise) 1 else -1) + underlying.size) % underlying.size
                return underlying[i]
            }
        }
    }

    override fun toString(): String {
        return "[VEO:" + (if (getEdgeDirections() === EdgeOrdering.MUTLIPLE_DIRECTIONS) "MULTI" else getEdgeDirections()) + ":" + getEdgesAsList() + "]"
    }
}