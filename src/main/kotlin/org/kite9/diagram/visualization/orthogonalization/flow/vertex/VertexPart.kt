package org.kite9.diagram.visualization.orthogonalization.flow.vertex

import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.visualization.orthogonalization.flow.vertex.ConstrainedVertexFlowOrthogonalizer.VertexDivision
import org.kite9.diagram.visualization.orthogonalization.flow.vertex.VertexPart

/**
 * Memento for part of a vertex in the flow graph.
 *
 * @author robmoffat
 */
data class VertexPart(val v: Vertex, val e: VertexDivision) {


}