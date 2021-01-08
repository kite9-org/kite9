package org.kite9.diagram.visualization.planarization.ordering

import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.model.position.Direction

/**
 * Defines the ordering of edges around a vertex and provides methods for changing the order.
 *
 * @author robmoffat
 */
interface VertexEdgeOrdering : EdgeOrdering {

    fun addEdgeDirection(d: Direction, isContradicting: Boolean)
    fun remove(toRemove: PlanarizationEdge)
    fun replace(b: PlanarizationEdge, a: PlanarizationEdge)
}