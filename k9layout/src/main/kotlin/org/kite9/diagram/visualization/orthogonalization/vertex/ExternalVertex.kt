package org.kite9.diagram.visualization.orthogonalization.vertex

import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.AbstractVertex
import org.kite9.diagram.common.elements.vertex.NoElementVertex
import org.kite9.diagram.model.DiagramElement

/**
 * Special marker vertex that allows us to represent the join points for
 * darts/vertices constructed by the VertexArranger.
 *
 * This keeps track of the underlying PLanarizationEdge that needs to meet from
 * the OrthBuilder process.
 *
 * @author robmoffat
 */
class ExternalVertex(id: String, private val joins: Set<PlanarizationEdge>, private val elements: Set<DiagramElement> = emptySet()) : AbstractVertex(id), NoElementVertex {

    override fun getDiagramElements(): Set<DiagramElement> {
        return elements
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return false
    }

    fun joins(e: PlanarizationEdge): Boolean {
        return joins.contains(e)
    }
}