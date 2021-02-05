package org.kite9.diagram.visualization.orthogonalization.flow

import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.vertex.AbstractVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.DiagramElement

data class EdgeVertex(val edge: Edge, val vertex: Vertex) {

    constructor(edge: Edge) : this(edge, NULL_VERTEX)

    companion object {
        private val NULL_VERTEX: Vertex = object : AbstractVertex("NULL") {
            override fun getDiagramElements(): Set<DiagramElement> {
                return emptySet()
            }

            override fun isPartOf(de: DiagramElement?): Boolean {
                return false
            }
        }
    }
}