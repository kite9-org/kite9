package org.kite9.diagram.common.elements

import org.kite9.diagram.common.objects.Bounds

/**
 * Stores some kind of information about a planarization position so
 * that we can compare it with another position.   Allows us to place
 * elements of the diagram relatively so we can score the layouts.
 *
 * @author robmoffat
 */
interface RoutingInfo : Comparable<RoutingInfo> {
    fun outputX(): String
    fun outputY(): String
    fun centerX(): Double
    fun centerY(): Double
    fun getBounds(d: Dimension) : Bounds
}