package org.kite9.diagram.visualization.planarization.ordering

import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.Tools.Companion.isUnderlyingContradicting

class BasicVertexEdgeOrdering(val underlying: MutableList<PlanarizationEdge>, val v: Vertex) : AbstractListBasedEdgeOrdering(), VertexEdgeOrdering {

    init {
        initDirections()
    }

    private fun initDirections() {
        directions = null
        for (edge in underlying) {
            addEdgeDirection(edge.getDrawDirectionFrom(v), isUnderlyingContradicting(edge))
        }
    }

    override fun remove(toRemove: PlanarizationEdge) {
        underlying.remove(toRemove)
        initDirections()
    }

    override fun replace(b: PlanarizationEdge, a: PlanarizationEdge) {
        val bIndex = underlying.indexOf(b)
        if (bIndex != -1) {
            underlying[bIndex] = a
        }
        changed()
    }

    override fun getEdgesAsList(): List<PlanarizationEdge> {
        return underlying
    }

    override fun getInterceptDirection(e: Edge): Direction {
        return e.getDrawDirectionFrom(v)!!
    }
}