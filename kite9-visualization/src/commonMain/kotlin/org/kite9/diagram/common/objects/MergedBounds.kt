package org.kite9.diagram.common.objects

import kotlin.math.max
import kotlin.math.min

/**
 * Used in cases where we are merging bounds.  Might not be kept forever.
 */
data class MergedBounds(override val distanceMin: Double, override val distanceMax: Double) : Bounds {

    override val distanceCenter: Double
        get() = (distanceMin + distanceMax) / 2.0

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

    /**
     * As well as following the usual -1, 0, 1 compare operation, this also returns the common level of the two bounds
     * being compared.
     */
    override operator fun compareTo(other: Bounds): Int {
        return if (distanceMax < other.distanceMin) {
            -1
        } else if (distanceMin > other.distanceMax) {
            1
        } else {
            0
        }
    }
}