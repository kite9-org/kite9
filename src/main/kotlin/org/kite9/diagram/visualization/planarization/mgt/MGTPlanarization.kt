package org.kite9.diagram.visualization.planarization.mgt

import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.visualization.planarization.rhd.RHDPlanarization

/**
 * Specific methods for GT-style planarization, in which the initial planarization is based on
 * an ordered list of vertices.
 *
 * @author moffatr
 */
interface MGTPlanarization : RHDPlanarization {
    fun isAdjacency(edge: Edge): Boolean
    fun crosses(edge: Edge, above: Boolean): Boolean
    fun crosses(i1: Float, i2: Float, above: Boolean): Boolean

    /**
     * Introduces a new vertex into the ordering after point i.
     */
    fun addVertexToOrder(i: Int, insert: Vertex)
    fun getVertexIndex(v: Vertex): Int
    val vertexOrder: List<Vertex>
    fun addEdge(toAdd: PlanarizationEdge, above: Boolean, outsideOf: PlanarizationEdge?)

    /**
     * For a given vertex, returns edges leaving vertex above the line of the planarization
     * going forwards, in inside-most to outside-most order.
     */
    fun getAboveForwardLinks(v: Vertex): List<PlanarizationEdge>
    fun getAboveBackwardLinks(v: Vertex): List<PlanarizationEdge>
    fun getBelowForwardLinks(v: Vertex): List<PlanarizationEdge>
    fun getBelowBackwardLinks(v: Vertex): List<PlanarizationEdge>
    val aboveLineEdges: Set<PlanarizationEdge>
    val belowLineEdges: Set<PlanarizationEdge>
    fun getFirstEdgeAfterPlanarizationLine(from: Vertex, forwardSet: Boolean, aboveSet: Boolean): PlanarizationEdge
}