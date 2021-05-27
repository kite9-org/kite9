package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.common.algorithms.det.Deterministic
import org.kite9.diagram.common.elements.ConstructionElement
import org.kite9.diagram.common.elements.Routable
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.model.DiagramElement

/**
 * Represents any positionable shape in the diagram, for topological
 * arrangers that don't have to care about what they are arranging.
 *
 * @author robmoffat
 *
 * @param <E>
</E> */
interface Vertex : Comparable<Vertex>, ConstructionElement, Routable, Deterministic {
    /**
     * True if this vertex is connected by an edge to v.
     */
    fun isLinkedDirectlyTo(v: Vertex): Boolean

    /**
     * Return true if the vertex has length and breadth.  False if it is a point vertex.
     */
    fun hasDimension(): Boolean
    fun getEdgeCount(): Int
    fun getEdges(): Iterable<Edge>
    fun removeEdge(e: Edge)
    fun addEdge(e: Edge)
    fun getDiagramElements(): Set<DiagramElement>
}