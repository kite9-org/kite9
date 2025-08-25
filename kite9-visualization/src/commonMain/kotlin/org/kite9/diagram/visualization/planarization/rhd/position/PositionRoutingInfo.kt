package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.elements.RoutingInfo

abstract class PositionRoutingInfo : RoutingInfo {

    abstract fun getMinX(): Double
    abstract fun getMaxX(): Double
    abstract fun getMinY(): Double
    abstract fun getMaxY(): Double
    abstract fun getWidth(): Double
    abstract fun getHeight(): Double

    override fun outputX(): String {
        return "${getMinX()}-${getMaxX()}"
    }

    override fun outputY(): String {
        return "${getMinY()}-${getMaxY()}"
    }

    override fun toString(): String {
        return outputX() + ", " + outputY()
    }

}