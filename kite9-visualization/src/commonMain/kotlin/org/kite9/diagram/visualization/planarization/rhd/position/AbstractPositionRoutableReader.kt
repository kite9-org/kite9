package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 *
 * A simple manhattan distance metric is used to identify the cost of the arrangement, so each
 * RoutingInfo is based on position. This class handles pretty much all of the cost arrangements and
 * distance calculations.
 */
abstract class AbstractPositionRoutableReader : RoutableHandler2D {

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

    override fun cost(from: Group, to: Group): Double {
        val fd = getPlacedPosition(from)
        val td = getPlacedPosition(to)
        return (minDist(fd.getMinX(), fd.getMaxX(), td.getMinX(), td.getMaxX()) +
                minDist(fd.getMinY(), fd.getMaxY(), td.getMinY(), td.getMaxY()))
    }
}
