package org.kite9.diagram.visualization.orthogonalization.vertex

import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization
import org.kite9.diagram.visualization.orthogonalization.contents.ContentsConverter

/**
 * The vertex arranger is part of the Orthogonalization process
 * where a vertex with dimensionality is converted into a number of darts
 *
 * @author robmoffat
 */
interface VertexArranger : ContentsConverter {

    interface TurnInformation {

        fun getFirstEdgeClockwiseEdgeOnASide(): Edge?

        /**
         * Direction of dart arriving at this vertex, after orthogonalization.
         */
        fun getIncidentDartDirection(e: Edge): Direction

        fun doesEdgeHaveTurns(e: Edge): Boolean
    }

    /**
     * Returns a subset of edges around the Rectangular perimeter which take you from the end of the incoming
     * connection edge to the start of the outgoing connection edge.
     */
    fun returnDartsBetween(
        peIn: PlanarizationEdge,
        outDirection: Direction,
        v: Vertex,
        peOut: PlanarizationEdge?,
        o: Orthogonalization,
        ti: TurnInformation
    ): List<DartDirection>

    /**
     * This is used for any vertex which is unconnected in the planarization
     */
    fun returnAllDarts(v: Vertex, o: Orthogonalization): List<DartDirection>

    /**
     * In the case of edge-crossing vertices etc.  we don't need to convert the vertex, so return false.
     */
    fun needsConversion(v: Vertex): Boolean
}