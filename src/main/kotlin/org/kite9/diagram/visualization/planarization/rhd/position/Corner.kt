/**
 *
 */
package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.objects.BasicBounds
import org.kite9.diagram.common.objects.Bounds

enum class Corner {

    TOP_RIGHT, BOTTOM_LEFT, FINISH;

    fun operate(
        from: BoundsBasedPositionRoutingInfo,
        ob: BoundsBasedPositionRoutingInfo
    ): BoundsBasedPositionRoutingInfo {
        return when (this) {
            TOP_RIGHT -> BoundsBasedPositionRoutingInfo(
                max(from.x, ob.x),
                min(from.y, ob.y)
            )
            BOTTOM_LEFT -> BoundsBasedPositionRoutingInfo(
                min(from.x, ob.x),
                max(from.y, ob.y)
            )
            FINISH -> BoundsBasedPositionRoutingInfo(
                narrow(from.x, ob.x),
                narrow(from.y, ob.y)
            )
            else -> from
        }
    }

    private fun max(a: Bounds, b: Bounds): Bounds {
        return BasicBounds(a.distanceMin.coerceAtLeast(b.distanceMax), a.distanceMax.coerceAtLeast(b.distanceMax))
    }

    private fun min(a: Bounds, b: Bounds): Bounds {
        return BasicBounds(a.distanceMin.coerceAtMost(b.distanceMin), a.distanceMax.coerceAtMost(b.distanceMin))
    }

    private fun narrow(a: Bounds, b: Bounds): Bounds {
        return if (a.distanceMax < b.distanceMin) {
            BasicBounds(b.distanceMin, b.distanceMin)
        } else if (a.distanceMin > b.distanceMax) {
            BasicBounds(b.distanceMax, b.distanceMax)
        } else {
            BasicBounds(a.distanceMin.coerceAtLeast(b.distanceMin), a.distanceMax.coerceAtMost(b.distanceMax))
        }
    }
}