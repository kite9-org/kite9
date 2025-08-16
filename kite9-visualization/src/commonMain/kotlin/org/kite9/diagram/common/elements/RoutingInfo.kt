package org.kite9.diagram.common.elements

/**
 * Stores some kind of information about the planarization position of a vertex in order that we
 * can route edges around it.
 *
 * @author robmoffat
 */
interface RoutingInfo : Comparable<RoutingInfo> {
    fun outputX(): String
    fun outputY(): String
    fun centerX(): Double
    fun centerY(): Double
    fun compareX(with: RoutingInfo): Int
    fun compareY(with: RoutingInfo): Int
}