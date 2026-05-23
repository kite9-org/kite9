package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.logging.LogicException

data class BoundsBasedPositionRoutingInfo(val x: Bounds, val y: Bounds) : PositionRoutingInfo() {

    override fun centerX(): Double {
        return x.distanceCenter
    }

    override fun centerY(): Double {
        return y.distanceCenter
    }

    override fun getBounds(d: Dimension): Bounds {
        return when (d) {
            Dimension.H -> x
            Dimension.V -> y
        }
    }

    override fun getMinX(): Double {
        return x.distanceMin
    }

    override fun getMaxX(): Double {
        return x.distanceMax
    }

    override fun getMinY(): Double {
        return y.distanceMin
    }

    override fun getMaxY(): Double {
        return y.distanceMax
    }

    override fun getWidth(): Double {
        return getMaxX() - getMinX()
    }

    override fun getHeight(): Double {
        return getMaxY() - getMinY()
    }

    override fun expandTo(o: PositionRoutingInfo): PositionRoutingInfo {
        if (o is BoundsBasedPositionRoutingInfo) {
            return BoundsBasedPositionRoutingInfo(x.expand(o.x), y.expand(o.y))
        } else {
            throw LogicException("not implemented")
        }
    }

    override fun compareTo(other: RoutingInfo): Int {
        val bbri = other as BoundsBasedPositionRoutingInfo
        val yc = y.compareTo(bbri.y)
        return if (yc != 0) {
            yc
        } else {
            x.compareTo(bbri.x)
        }
    }

    override fun toString(): String {
        return "[x=$x, y=$y]"
    }


}
