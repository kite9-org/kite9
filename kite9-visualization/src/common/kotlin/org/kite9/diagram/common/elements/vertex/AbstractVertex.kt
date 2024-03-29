package org.kite9.diagram.common.elements.vertex

import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.elements.edge.Edge
import org.kite9.diagram.model.position.CostedDimension2D
import org.kite9.diagram.model.position.Dimension2D

/**
 * Helper class for implementations of Vertex.  Ids are immutable on each vertex, and
 * are set on the constructor only.
 */
abstract class AbstractVertex(private val id: String) : Vertex {

    override fun getID(): String {
        return id
    }

    /**
     * Defaults to false, but Arrows and Glyphs override to true
     */
    override fun hasDimension(): Boolean {
        return false
    }

    private val edges = ArrayList<Edge>();

    override fun getEdges(): MutableList<Edge>  {
        return edges;
    }

    override operator fun compareTo(other: Vertex): Int {
        return getID().compareTo(other.getID())
    }

    override fun getEdgeCount(): Int {
        return edges.size
    }

    override fun isLinkedDirectlyTo(v: Vertex): Boolean {
        for (link in getEdges()) {
            if (link.getFrom() === v || link.getTo() === v) return true
        }
        return false
    }

    override fun toString(): String {
        return "[V:" + getID() + "]"
    }

    override fun removeEdge(e: Edge) {
        edges.remove(e)
    }

    override fun addEdge(e: Edge) {
        if (!edges.contains(e)) {
            edges.add(e)
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }


    override var routingInfo: RoutingInfo? = null

}