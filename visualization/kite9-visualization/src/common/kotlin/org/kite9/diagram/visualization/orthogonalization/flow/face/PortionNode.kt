package org.kite9.diagram.visualization.orthogonalization.flow.face

import org.kite9.diagram.common.algorithms.fg.Arc
import org.kite9.diagram.common.algorithms.fg.SimpleNode
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.visualization.orthogonalization.flow.AbstractFlowOrthogonalizer
import org.kite9.diagram.visualization.planarization.Face

/**
 * A portion models a part of a face that must contain a certain number of corners. The number of corners is allocated
 * by the constraint system. Portions start and end with constrained edges (i.e. edges where a direction is set), unless
 * they represent a whole face without constrained edges.
 *
 */
class PortionNode(id: String, supply: Int, val face: Face,
                  val edgeStartPosition: Int = -1, val edgeEndPosition: Int = -1) : SimpleNode(
    id, supply, null
) {
    var faceArc: Arc? = null

    /**
     * Returns true if the vertex is within the boundary of the face covered by this portion
     */
    fun containsVertexForEdge(e: Edge, v: Vertex): Boolean {
        if (containsInPortion(e)) {
            return true
        }
        if (edgeStartPosition == edgeEndPosition) {
            // only one edge
            return false
        }
        return if (face.getBoundary(edgeStartPosition) === e) {
            // start edge
            val startVertex = face.getCorner(edgeStartPosition + 1)
            v === startVertex
        } else if (face.getBoundary(edgeEndPosition) === e) {
            val endVertex = face.getCorner(edgeEndPosition)
            v === endVertex
        } else {
            throw LogicException("edge $e not in portion: $this")
        }
    }

    private fun containsInPortion(e: Edge): Boolean {
        return if (edgeStartPosition == -1) {
            true
        } else {
            val ep = face.indexOf(e)
            if (ep.size != 1) {
                throw LogicException("Was expecting edge to only appear once in face: $e")
            }
            val first = ep[0]
            if (first > edgeStartPosition) {
                if (edgeEndPosition < edgeStartPosition) {
                    return true
                } else if (first < edgeEndPosition) {
                    return true
                }
            }
            false
        }
    }

    fun containsFacePart(i: Int): Boolean {
        if (edgeStartPosition == -1) return true
        if (i >= edgeStartPosition) {
            if (i <= edgeEndPosition) {
                return true
            } else if (edgeEndPosition < edgeStartPosition) {
                return true
            }
        } else {
            if (edgeEndPosition < edgeStartPosition) {
                if (i <= edgeEndPosition) {
                    return true
                }
            }
        }
        return false
    }

    val constrainedEdgeStart: Edge?
        get() = if (edgeStartPosition == -1) null else face.getBoundary(edgeStartPosition)
    val constrainedEdgeEnd: Edge?
        get() = if (edgeEndPosition == -1) null else face.getBoundary(edgeEndPosition)

    fun getEdge(i: Int): Edge {
        return if (edgeStartPosition != -1) {
            face.getBoundary(i + edgeStartPosition)
        } else {
            face.getBoundary(i)
        }
    }

    init {
        type = AbstractFlowOrthogonalizer.PORTION_NODE
    }
}