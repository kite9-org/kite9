package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.visualization.planarization.mgt.router.LineRoutingInfo
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader.Routing
import kotlin.math.abs

class LinePositionRoutingInfo(from: LinePositionRoutingInfo?, pri: BoundsBasedPositionRoutingInfo, r: Routing?) :
    LineRoutingInfo {

    private val horizontalRunningCost : Double
    private val verticalRunningCost : Double
    val positionForTesting: BoundsBasedPositionRoutingInfo
    private val obstacle: BoundsBasedPositionRoutingInfo?
    private val r: Routing?

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

    override fun getHorizontalRunningCost(): Double {
        return horizontalRunningCost
    }

    override fun getVerticalRunningCost(): Double {
        return verticalRunningCost
    }

    override fun getRunningCost(): Double {
        return horizontalRunningCost + verticalRunningCost
    }

    override fun toString(): String {
        return "[x=" + positionForTesting!!.x + ", y=" + positionForTesting!!.y + ", c=" + PositionRoutingInfo.nf.format(
            getRunningCost()
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
        var _positionForTesting : BoundsBasedPositionRoutingInfo? = null
        var _horizontalRunningCost : Double = 0.0
        var _verticalRunningCost: Double = 0.0
        var _obstacle : BoundsBasedPositionRoutingInfo? = null
        var _r : Routing? = null

        fun avoid(r: Routing?, obstacle: BoundsBasedPositionRoutingInfo, next: BoundsBasedPositionRoutingInfo?) {
            val frr = _positionForTesting
            val c = getCorner(r, obstacle, next)
            _positionForTesting = c.operate(frr!!, obstacle)
            _horizontalRunningCost += xCost(frr, _positionForTesting)
            _verticalRunningCost += yCost(frr, _positionForTesting)
        }

        if (from == null) {
            _positionForTesting = pri
        } else {
            _horizontalRunningCost = from.horizontalRunningCost
            _verticalRunningCost = from.verticalRunningCost
            _positionForTesting = from.positionForTesting
            val ob = from.obstacle
            if (ob != null) {
                avoid(from.r, ob, pri)
            }
            if (r == null) {
                avoid(r, pri, null)
            } else {
               _obstacle = pri
                _r = r
            }
        }

        horizontalRunningCost = _horizontalRunningCost
        verticalRunningCost = _verticalRunningCost
        positionForTesting = _positionForTesting!!
        obstacle = _obstacle
        this.r = _r
    }
}