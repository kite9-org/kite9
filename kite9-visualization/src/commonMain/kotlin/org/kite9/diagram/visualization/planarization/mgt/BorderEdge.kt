package org.kite9.diagram.visualization.planarization.mgt

import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge.RemovalType
import org.kite9.diagram.common.elements.edge.TwoElementPlanarizationEdge
import org.kite9.diagram.common.elements.vertex.EdgeCrossingVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.rotateAntiClockwise
import org.kite9.diagram.model.style.BorderTraversal

/**
 * This edge is used for the surrounding of a diagram element.
 *
 * Since all diagrams are containers of vertices, these edges will be used around the perimeter of the diagram.
 *
 * The border edge keeps track of the rectangular border.  You can work out from their containment which side is which.
 *
 * Borders only keep track of outer edges of containers.  So forElements will only return an element if the border is
 * one of the four outer sides of the container.
 *
 * @author robmoffat
 */
class BorderEdge(
    from: Vertex,
    to: Vertex,
    var label: String,
    d: Direction,
    private val forElements: MutableMap<DiagramElement, Direction?>
) : AbstractPlanarizationEdge(
    from, to, d
), TwoElementPlanarizationEdge {
    /**
     * For a given diagram element, shows what side of that element this edge is on.
     */
    override fun getDiagramElements(): MutableMap<DiagramElement, Direction?> {
        return forElements
    }

    override fun toString(): String {
        return label
    }

    override fun getCrossCost(): Int {
        return 0
    }

    override fun getDrawDirection(): Direction {
        return super.getDrawDirection()!!
    }

    override fun removeBeforeOrthogonalization(): RemovalType {
        return RemovalType.NO
    }

    override fun isLayoutEnforcing(): Boolean {
        return false
    }

    override fun setLayoutEnforcing(le: Boolean) {
        throw UnsupportedOperationException("Container edges are never layout enforcing")
    }

    override fun split(toIntroduce: Vertex): Pair<PlanarizationEdge> {
        val out = Pair<PlanarizationEdge>(
            BorderEdge(getFrom(), toIntroduce, label + "_1", getDrawDirection(), forElements),
            BorderEdge(toIntroduce, getTo(), label + "_2", getDrawDirection(), forElements))

        if (toIntroduce is EdgeCrossingVertex) {
            // track the containers that we are involved in
            for (c in forElements.keys) {
                toIntroduce.addUnderlying(c)
            }
        }

        return out
    }

    val borderTraversal: BorderTraversal? by lazy {
            calculateTraversalRule()
        }

    private fun calculateTraversalRule(): BorderTraversal? {
        var out: BorderTraversal? = null
        for ((underlying) in forElements) {
            if (underlying is Container) {
                val bt = underlying.getTraversalRule(
                    rotateAntiClockwise(
                        getDrawDirection()!!
                    )
                )
                out = BorderTraversal.reduce(out, bt)
            }
        }
        return out
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return forElements.containsKey(de)
    }

    override fun getElementForSide(d: Direction): DiagramElement? {
        for (de in forElements.keys) {
            if (forElements[de] === d) {
                return de
            }
        }
        return null
    }
}