package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.elements.RoutingInfo

abstract class PositionRoutingInfo : RoutingInfo {

    abstract val minX: Double
    abstract val maxX: Double
    abstract val minY: Double
    abstract val maxY: Double
    abstract val width: Double
    abstract val height: Double
    abstract val isBreakingOrder: Boolean

    override fun outputX(): String {
        return "${minX}-${maxX}"
    }

    override fun outputY(): String {
        return "${minY}-${maxY}"
    }

    override fun toString(): String {
        return outputX() + ", " + outputY()
    }

}