package org.kite9.diagram.visualization.orthogonalization.edge

import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.position.Direction

/**
 * Holds info about the darts due to an incoming edge.
 */
class IncidentDart(
    var external: Vertex,
    val internal: Vertex,
    val arrivalSide: Direction,
    val dueTo: PlanarizationEdge
) {
    override fun toString(): String {
        return "IncidentDart[edge=$dueTo, arrivalSide=$arrivalSide, internal=$internal, external=$external]"
    }
}