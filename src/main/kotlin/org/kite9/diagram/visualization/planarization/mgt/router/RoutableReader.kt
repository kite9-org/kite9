package org.kite9.diagram.visualization.planarization.mgt.router

import org.kite9.diagram.common.elements.RoutingInfo

/**
 * Handles [Routable] for each element in the group process, where routables are the basic unit of
 * currency for the [AbstractRouteFinder].
 *
 * @author robmoffat
 */
interface RoutableReader
{
    fun getPlacedPosition(r: Any): RoutingInfo?

    enum class Routing {
        OVER_FORWARDS, UNDER_FORWARDS, OVER_BACKWARDS, UNDER_BACKWARDS
    }

    /**
     * Performs a move operation, where the move is either above or below 'past'.
     * linePosition can be provided null, in which case we are setting up a new route starting at past.
     * If r is null, we are arriving at a destination.
     */
    fun move(current: LineRoutingInfo?, past: RoutingInfo, r: Routing?): LineRoutingInfo
    fun isWithin(area: RoutingInfo, pos: RoutingInfo): Boolean

    /**
     * Checks that to and from occupy the same horiz/vert plane
     */
    fun isInPlane(to: RoutingInfo, from: RoutingInfo, horiz: Boolean): Boolean

    /**
     * Creates a space containing all the area from/to
     */
    fun increaseBounds(a: RoutingInfo, b: RoutingInfo): RoutingInfo

    /**
     * Returns true if there is a common intersection
     */
    fun overlaps(a: RoutingInfo, b: RoutingInfo): Boolean
    fun initRoutableOrdering(items: List<Any>)
}