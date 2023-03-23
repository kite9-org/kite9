package org.kite9.diagram.visualization.orthogonalization.flow

import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.visualization.planarization.Face

data class FaceVertex (val face: Face, val vertex: Vertex, val prior: PlanarizationEdge,val after: PlanarizationEdge) {

    override fun toString(): String {
        return face!!.getID() + "-" + vertex!!.getID() + "-" + prior + getUnderlying(prior) + "-" + after + getUnderlying(
            after
        )
    }

    private fun getUnderlying(e: PlanarizationEdge): String {
        return e.getDiagramElements().keys.toString()
    }
}