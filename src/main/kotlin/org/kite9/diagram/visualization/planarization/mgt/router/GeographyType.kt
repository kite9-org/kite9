package org.kite9.diagram.visualization.planarization.mgt.router

/**
 * Describes how we are allowed to route the edge with respect to the geographic
 * positions of the element's [RoutingInfo].
 *
 * STRICT means that the elements must be separated, and there must be space between them
 * in the expected direction for the edge to travel.
 *
 * @author robmoffat
 */
enum class GeographyType {
    STRICT, RELAXED
}