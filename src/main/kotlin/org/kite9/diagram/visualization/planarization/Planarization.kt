package org.kite9.diagram.visualization.planarization

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering

/**
 * A [Planarization] is a one dimensional ordering of Vertices, as well as the ordering of edges around each vertex
 * and the dual (i.e list of faces).
 *
 * @author robmoffat
 */
interface Planarization {
    /**
     * Returns the ordering of vertices within the Planarization
     */
    val allVertices: MutableCollection<Vertex>

    /**
     * Returns the list of faces within the Planarization
     */
    val faces: MutableList<Face>

    /**
     * Each edge connects to one face, or two faces after the temporary directed edge are inserted.
     */
    val edgeFaceMap: MutableMap<Edge, MutableList<Face>>

    /**
     * Gets the details of which vertices belong to which faces.
     */
    val vertexFaceMap: MutableMap<Vertex, MutableList<Face>>

    /**
     * Needs to return the clockwise ordering of edges meeting a vertex or a container.
     */
    val edgeOrderings: MutableMap<Vertex, EdgeOrdering>

    /**
     * Returns all edges in the planarization.
     */
    val allEdges: MutableList<Edge>

    /**
     * Any connections or layout that haven't yet, or can't be introduced into the planar embedding.
     */
    val uninsertedConnections: MutableCollection<BiDirectional<Connected>>

    /**
     * Simply removes the edge from a planarization, without respect for any
     * data structures.
     */
    fun removeEdge(e: Edge)

    /**
     * Manages the mapping of edges to diagram attr.
     */
    val edgeMappings: MutableMap<DiagramElement, EdgeMapping>

    /**
     * Returns the diagram itself
     */
    val diagram: Diagram

    /**
     * Creates an empty face in the planarization
     */
    fun createFace(): Face

    /**
     * Returns details about where the attr in the diagram have been placed for routing purposes
     */
    fun getPlacedPosition(de: DiagramElement): RoutingInfo?
}