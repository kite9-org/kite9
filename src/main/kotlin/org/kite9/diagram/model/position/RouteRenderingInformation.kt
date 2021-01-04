package org.kite9.diagram.model.position

/**
 * This is used to hold the route of an edge, or container border.
 *
 * @author robmoffat
 */
interface RouteRenderingInformation : RenderingInformation {
    val routePositions: List<Dimension2D>?
    val hops: List<Boolean>?
    fun getWaypoint(pos: Int): Dimension2D?
    fun clear()
    fun size(): Int
    fun add(d: Dimension2D)
    fun isHop(pos: Int): Boolean
    var isContradicting: Boolean
}