package org.kite9.diagram.common.objects

import kotlin.math.max
import kotlin.math.min

enum class Division {
    FIRST_HALF, SECOND_HALF
}

data class BasicBounds(
    val parent: BasicBounds?,
    val div: Division?,
    val depth : Int,
    val range : ClosedFloatingPointRange<Double>) : Bounds {

    constructor() : this(null, null, 0, 0.0 .. 1.0)

    constructor(parent: BasicBounds, div: Division) : this(parent, div, parent.depth+1, calcRange(parent, div))

    override val distanceMax: Double
        get() = this.range.endInclusive

    override val distanceMin: Double
        get() = this.range.start

    override val distanceCenter: Double
        get() = (distanceMax + distanceMin) / 2.0

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

    override fun toString(): String {
        return "[$distanceMin - $distanceMax]"
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

    companion object {

        val EMPTY_BOUNDS = BasicBounds()

        fun extendBounds(parent: BasicBounds, division: Division?) : BasicBounds {
            if (division != null) {
                return BasicBounds(parent, division)
            } else {
                return parent
            }
        }

        fun calcRange(parent: BasicBounds, div: Division) : ClosedFloatingPointRange<Double> {
            val halfway = (parent.range.start + parent.range.endInclusive) / 2.0
            return when (div) {
                Division.FIRST_HALF -> parent.range.start .. halfway
                Division.SECOND_HALF -> halfway .. parent.range.endInclusive
            }
        }

    }
}