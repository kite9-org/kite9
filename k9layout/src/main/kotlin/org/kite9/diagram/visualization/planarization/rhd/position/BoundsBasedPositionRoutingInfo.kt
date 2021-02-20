package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader.Routing

class BoundsBasedPositionRoutingInfo(val x: Bounds, val y: Bounds) : PositionRoutingInfo() {

    override fun centerX(): Double {
        return x.distanceCenter
    }

    override fun centerY(): Double {
        return y.distanceCenter
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


    override fun compareTo(arg0: RoutingInfo): Int {
        val bbri = arg0 as BoundsBasedPositionRoutingInfo
        val yc = y.compareTo(bbri.y)
        return if (yc != 0) {
            yc
        } else {
            x.compareTo(bbri.x)
        }
    }

    override fun compareX(with: RoutingInfo): Int {
        return x.compareTo((with as BoundsBasedPositionRoutingInfo).x)
    }

    override fun compareY(with: RoutingInfo): Int {
        return y.compareTo((with as BoundsBasedPositionRoutingInfo).y)
    }

    var avoidanceCorners: Map<Routing, Corner>? = null

}