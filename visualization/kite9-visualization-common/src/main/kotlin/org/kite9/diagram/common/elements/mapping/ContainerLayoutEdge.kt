package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge.RemovalType
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction

/**
 * This edge is used to enforce a particular layout within a container.
 *
 * It is used between the sibling contents of the container.
 *
 * @author robmoffat
 */
class ContainerLayoutEdge(
    f: Vertex,
    t: Vertex,
    dd: Direction,
    val underlying: GeneratedLayoutBiDirectional,
    override var fromUnderlying: DiagramElement?,
    override var toUnderlying: DiagramElement?) :
    AbstractPlanarizationEdge(f, t, dd),
    BiDirectionalPlanarizationEdge {

    constructor(from: Vertex, to: Vertex, d: Direction, fromElement: Connected, toElement: Connected) : this(
        from,
        to,
        d,
        GeneratedLayoutBiDirectional(fromElement, toElement, d),
        fromElement,
        toElement
    ) {
    }

    override fun getOriginalUnderlying(): DiagramElement {
        return underlying
    }

    override fun getCrossCost(): Int {
        return 0 // no cost for traversing between items in the layout
    }

    override fun removeBeforeOrthogonalization(): RemovalType {
        // can be removed if there is another edge to do the same job
        return RemovalType.TRY
    }

    override fun isLayoutEnforcing(): Boolean {
        return true
    }

    override fun setLayoutEnforcing(le: Boolean) {
        throw UnsupportedOperationException("Layout edges are always layout enforcing")
    }

    override fun split(toIntroduce: Vertex): Pair<PlanarizationEdge> {
        val out = Pair<PlanarizationEdge>(
            ContainerLayoutEdge(
                getFrom(),
                toIntroduce,
                getDrawDirection(),
                underlying,
                fromUnderlying,
                null),
            ContainerLayoutEdge(
                toIntroduce,
                getTo(),
                getDrawDirection(),
                underlying,
                null,
                toUnderlying));
        return out
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return getOriginalUnderlying() === de
    }

    override fun getDiagramElements(): MutableMap<DiagramElement, Direction?> {
        return mutableMapOf(kotlin.Pair(getOriginalUnderlying(), null))
    }

    override fun getDrawDirection() : Direction {
        return super.getDrawDirection()!!
    }

    init {
        setStraight(true)
    }
}