package org.kite9.diagram.common.elements.mapping

import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge
import org.kite9.diagram.common.elements.edge.PlanarizationEdge.RemovalType
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.common.objects.Pair
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction

/**
 * This edge is created by the [Planarizer] to represent a connection.
 *
 * @author robmoffat
 */
class ConnectionEdge(
    f: Vertex,
    t: Vertex,
    d: Direction?,
    private var fas: Direction?,
    private var tas: Direction?,
    straightIn: Boolean,
    val underlying: Connection,
    var fromUnderlying: Connected?,
    var toUnderlying: Connected?
) : AbstractPlanarizationEdge(
    f, t, d
), BiDirectionalPlanarizationEdge {

    constructor(from: Vertex, to: Vertex, underlying: Connection, d: Direction?) : this(
        from,
        to,
        d,
        underlying.getFromArrivalSide(),
        underlying.getToArrivalSide(),
        d != null,
        underlying,
        underlying.getFrom(),
        underlying.getTo()
    )

    override fun getOriginalUnderlying(): Connection {
        return underlying
    }

    override fun getCrossCost(): Int {
        return 500
    }

    override fun removeBeforeOrthogonalization(): RemovalType {
        return RemovalType.NO
    }

    private var layoutEnforcing = false
    override fun isLayoutEnforcing(): Boolean {
        return layoutEnforcing
    }

    override fun setLayoutEnforcing(le: Boolean) {
        layoutEnforcing = le
    }

    override fun split(toIntroduce: Vertex): Pair<PlanarizationEdge> {
        val out = Pair<PlanarizationEdge>(
            ConnectionEdge(
                getFrom(),
                toIntroduce,
                getDrawDirection(),
                getFromArrivalSide(),
                getToArrivalSide(),
                isStraightInPlanarization(),
                getOriginalUnderlying(),
                getFromConnected(),
                null
            ),
            ConnectionEdge(
                toIntroduce,
                getTo(),
                getDrawDirection(),
                getFromArrivalSide(),
                getToArrivalSide(),
                isStraightInPlanarization(),
                getOriginalUnderlying(),
                null,
                getToConnected()
            )
        )
        return out
    }

    override fun isPartOf(de: DiagramElement?): Boolean {
        return getOriginalUnderlying() === de
    }

    override fun getDiagramElements(): MutableMap<DiagramElement, Direction?> {
        return mutableMapOf(kotlin.Pair(getOriginalUnderlying(), null))
    }

    override fun getFromConnected(): Connected? {
        return fromUnderlying
    }

    override fun getToConnected(): Connected? {
        return toUnderlying
    }

    fun setFromConnected(c: Connected?) {
        fromUnderlying = c
    }

    fun setToConnected(c: Connected?) {
        toUnderlying = c
    }

    override fun getFromArrivalSide(): Direction? {
        return fas ?: super.getFromArrivalSide()
    }

    override fun getToArrivalSide(): Direction? {
        return tas ?: super.getToArrivalSide()
    }

    init {
        setStraight(straightIn)
    }
}