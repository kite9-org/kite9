package org.kite9.diagram.visualization.planarization.rhd.position

import kotlin.math.max
import kotlin.math.min
import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader

/**
 *
 * A simple manhattan distance metric is used to identify the cost of the arrangement, so each
 * RoutingInfo is based on position. This class handles pretty much all of the cost arrangements and
 * distance calculations.
 */
abstract class AbstractPositionRoutableReader : RoutableReader, RoutableHandler2D {

    private fun minDist(min1: Double, max1: Double, min2: Double, max2: Double): Double {
        return if (min2 > min1) {
            if (min2 < max1) {
                0.0
            } else {
                min2 - max1
            }
        } else {
            if (max2 > min1) {
                0.0
            } else {
                min1 - max2
            }
        }
    }

    override fun cost(from: RoutingInfo, to: RoutingInfo): Double {
        val fd = from as PositionRoutingInfo
        val td = to as PositionRoutingInfo
        return (minDist(fd.getMinX(), fd.getMaxX(), td.getMinX(), td.getMaxX()) +
                minDist(fd.getMinY(), fd.getMaxY(), td.getMinY(), td.getMaxY()))
    }

    private fun narrow(a1: Double, a2: Double, b1: Double, b2: Double): DoubleArray {
        return if (a2 < b1) {
            doubleArrayOf(b1, b1, b1 - a2)
        } else if (a1 > b2) {
            doubleArrayOf(b2, b2, a1 - b2)
        } else {
            doubleArrayOf(max(a1, b1), min(a2, b2), 0.0)
        }
    }

    override fun emptyBounds(): RoutingInfo {
        return EMPTY_BOUNDS
    }

    override fun isEmptyBounds(bounds: RoutingInfo): Boolean {
        return bounds === EMPTY_BOUNDS
    }

    override fun isInPlane(to: RoutingInfo, from: RoutingInfo, horiz: Boolean): Boolean {
        val pto = to as PositionRoutingInfo
        val pfrom = from as PositionRoutingInfo
        return if (!horiz) {
            checkHorizontal(pto, pfrom)
        } else {
            checkVertical(pto, pfrom)
        }
    }

    private fun checkHorizontal(pto: PositionRoutingInfo, pfrom: PositionRoutingInfo): Boolean {
        return narrow(pto.getMinX(), pto.getMaxX(), pfrom.getMinX(), pfrom.getMaxX())[2] == 0.0
    }

    private fun checkVertical(pto: PositionRoutingInfo, pfrom: PositionRoutingInfo): Boolean {
        return narrow(pto.getMinY(), pto.getMaxY(), pfrom.getMinY(), pfrom.getMaxY())[2] == 0.0
    }

    companion object {
        val EMPTY_BOUNDS: PositionRoutingInfo =
                object : PositionRoutingInfo() {
                    override fun centerY(): Double {
                        return 0.0
                    }

                    override fun getMinX(): Double {
                        return 0.0
                    }

                    override fun getMaxX(): Double {
                        return 0.0
                    }

                    override fun getMinY(): Double {
                        return 0.0
                    }

                    override fun getMaxY(): Double {
                        return 0.0
                    }

                    override fun getWidth(): Double {
                        return 0.0
                    }

                    override fun getHeight(): Double {
                        return 0.0
                    }

                    override fun centerX(): Double {
                        return 0.0
                    }

                    override fun compareTo(other: RoutingInfo): Int {
                        throw UnsupportedOperationException("Can't compare empty bounds")
                    }

                    override fun compareX(with: RoutingInfo): Int {
                        throw UnsupportedOperationException("Can't compare empty bounds")
                    }

                    override fun compareY(with: RoutingInfo): Int {
                        throw UnsupportedOperationException("Can't compare empty bounds")
                    }
                }
    }
}
