package org.kite9.diagram.visualization.orthogonalization

import org.kite9.diagram.common.elements.AbstractBiDirectional
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.position.Direction

/**
 * A Dart represents a horizontal or vertical extent of an edge or shape perimeter.
 * *
 * Darts also have the 'fixedLength' identifier.  This is set for darts representing
 * the perimeter of vertices, as we would like rectangularization not to increase the
 * length of the dart.
 *
 * Darts also have a orthogonal position preference direction.  This is used when compacting
 * to indicate whether the dart wants to be pushed orthogonally in a particular
 * direction.
 *
 *
 * @author robmoffat
 */
internal data class DartImpl(
    val f: Vertex,
    val t: Vertex,
    val underlyings: MutableMap<DiagramElement, Direction>,
    val d: Direction,
    val id: String,
    val o: OrthogonalizationImpl
) : AbstractBiDirectional<Vertex>(), Dart {

    private val hashCode: Int

    override fun toString(): String {
        return "[" + getFrom() + "-" + getTo() + "-" + d + "]"
    }

    override fun getDiagramElements(): MutableMap<DiagramElement, Direction> {
        return underlyings
    }

    override fun getID(): String {
        return id
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return underlyings.containsKey(de)
    }

    override fun getFrom(): Vertex {
        return f
    }

    override fun getTo(): Vertex {
        return t
    }

    /**
     * Ensures the identity of the edge doesn't change when we alter one of it's endpoints
     */
    override fun hashCode(): Int {
        return hashCode;
    }

    /**
     * Constructor is in Orthogonalization
     */
    init {
        f.addEdge(this)
        t.addEdge(this)
        hashCode = id.hashCode()
    }

    override fun getDrawDirection() : Direction {
        return d
    }
}