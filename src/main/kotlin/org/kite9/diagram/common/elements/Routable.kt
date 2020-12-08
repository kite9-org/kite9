package org.kite9.diagram.common.elements

/**
 * The [SSPEdgeRouter] requires that vertices implement Routable so that it can route edges
 * correctly around them.
 *
 * @author robmoffat
 */
interface Routable {
    var routingInfo: RoutingInfo
}