package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.visualization.planarization.mgt.router.LineRoutingInfo
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader.Routing
import kotlin.math.abs

class LinePositionRoutingInfo(from: LinePositionRoutingInfo?, pri: BoundsBasedPositionRoutingInfo, r: Routing?) :
    LineRoutingInfo {

    override var horizontalRunningCost = 0.0
        private set
    override var verticalRunningCost = 0.0
        private set

    var positionForTesting: BoundsBasedPositionRoutingInfo? = null

    var obstacle: BoundsBasedPositionRoutingInfo? = null

    var r: Routing? = null

    private fun avoid(r: Routing?, obstacle: BoundsBasedPositionRoutingInfo, next: BoundsBasedPositionRoutingInfo?) {
        val frr = positionForTesting
        val c = getCorner(r, obstacle, next)
        positionForTesting = c!!.operate(frr!!, obstacle!!)
        horizontalRunningCost += xCost(frr, positionForTesting)
        verticalRunningCost += yCost(frr, positionForTesting)
    }

    private fun getCorner(
        r: Routing?,
        ob: BoundsBasedPositionRoutingInfo,
        next: BoundsBasedPositionRoutingInfo?
    ): Corner {
        val c = Corner.FINISH
        return if (r != null) {
            ob.avoidanceCorners[r]!!
        } else c
    }

    override val runningCost: Double
        get() = horizontalRunningCost + verticalRunningCost

    override fun toString(): String {
        return "[x=" + positionForTesting!!.x + ", y=" + positionForTesting!!.y + ", c=" + PositionRoutingInfo.nf.format(
            runningCost
        ) + "]"
    }

    private fun xCost(a: BoundsBasedPositionRoutingInfo?, b: BoundsBasedPositionRoutingInfo?): Double {
        return cost(a!!.minX, a.maxX, b!!.minX, b.maxX)
    }

    private fun yCost(a: BoundsBasedPositionRoutingInfo?, b: BoundsBasedPositionRoutingInfo?): Double {
        return cost(a!!.minY, a.maxY, b!!.minY, b.maxY)
    }

    private fun cost(ps: Double, pe: Double, das: Double, ae: Double): Double {
        return if (pe < das && pe < ae) {
            abs(pe - das.coerceAtMost(ae))
        } else if (ps > das && ps > ae) {
            abs(ps - das.coerceAtLeast(ae))
        } else {
            0.0
        }
    }

    init {
        if (from == null) {
            positionForTesting = pri
        } else {
            horizontalRunningCost = from.horizontalRunningCost
            verticalRunningCost = from.verticalRunningCost
            positionForTesting = from.positionForTesting
            val ob = from.obstacle
            if (ob != null) {
                avoid(from.r, ob, pri)
            }
            if (r == null) {
                avoid(r, pri, null)
            } else {
                obstacle = pri
                this.r = r
            }
        }
    }
}