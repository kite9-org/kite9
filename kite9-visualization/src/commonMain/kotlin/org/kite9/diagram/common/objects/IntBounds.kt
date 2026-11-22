package org.kite9.diagram.common.objects

import kotlin.math.max
import kotlin.math.min

class IntBounds(val i: Int) : Bounds {

    override val distanceMin: Double
        get() = i.toDouble()

    override val distanceMax: Double
        get() = (i+1).toDouble()

    override val distanceCenter: Double
        get() = i.toDouble() + 0.5

    override fun expand(other: Bounds): Bounds {
        return MergedBounds(min(this.distanceMin, other.distanceMin), max(this.distanceMax, other.distanceMax))
    }

    override fun compareBounds(other: Bounds): DPos {
        return if (this.distanceMax <= other.distanceMin) {
            DPos.BEFORE
        } else if (this.distanceMin >= other.distanceMax) {
            DPos.AFTER
        } else {
            DPos.OVERLAP
        }
    }

    override fun compareTo(other: Bounds): Int {
        return if (distanceMax < other.distanceMin) {
            -1
        } else if (distanceMin > other.distanceMax) {
            1
        } else {
            0
        }
    }

}