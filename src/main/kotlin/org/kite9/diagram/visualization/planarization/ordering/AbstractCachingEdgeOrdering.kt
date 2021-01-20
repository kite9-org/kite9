package org.kite9.diagram.visualization.planarization.ordering

import org.kite9.diagram.common.elements.edge.PlanarizationEdge

/**
 * Used where the underlying order is not based on a list, but one would nevertheless come in handy.
 *
 * @author robmoffat
 */
abstract class AbstractCachingEdgeOrdering : AbstractEdgeOrdering() {

    private var cache: List<PlanarizationEdge>? = null

    override fun getEdgesAsList(): List<PlanarizationEdge> {
        if (cache == null) {
            cache = edgesAsListInner.toList()
        }
        return cache!!
    }

    protected abstract val edgesAsListInner: List<PlanarizationEdge>

    override fun getEdgeDirections(): Any? {
        // prepare the cache first
        getEdgesAsList()
        return super.getEdgeDirections()
    }

    override fun changed() {
        cache = null
        super.changed()
    }

    override fun size(): Int {
        return getEdgesAsList().size
    }

    override fun getIterator(
        clockwise: Boolean,
        startingAt: PlanarizationEdge,
        finish: PlanarizationEdge?,
        directedOnly: Boolean
    ): Iterator<PlanarizationEdge> {
        val underlying = getEdgesAsList()
        return object : AbstractEdgeIterator(clockwise, startingAt, finish, directedOnly) {
            var i = underlying.indexOf(startingAt)
            override fun getNext(): PlanarizationEdge {
                i = (i + (if (clockwise) 1 else -1) + underlying.size) % underlying.size
                return underlying[i]
            }
        }
    }
}