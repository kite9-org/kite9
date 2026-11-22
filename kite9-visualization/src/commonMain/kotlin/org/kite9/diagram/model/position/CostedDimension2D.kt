package org.kite9.diagram.model.position

import kotlin.math.max

/**
 * This extends the idea of dimension, but allows you to associate a cost with the dimension other
 * than simply the size.
 *
 * @author robmoffat
 */
data class CostedDimension2D(override val w: Double, override val h: Double, val cost: Long) :
        Dimension2D, Comparable<CostedDimension2D> {

    constructor(d: Dimension2D) : this(d.width(), d.height(), 0)
    constructor(width: Double, height: Double) : this(width, height, 0)
    constructor(
            width: Double,
            height: Double,
            within: Dimension2D
    ) : this(width, height, calcCost(width, height, within))

    override fun compareTo(other: CostedDimension2D): Int {
        return cost.compareTo(other.cost)
    }

    companion object {

        val ZERO: CostedDimension2D = CostedDimension2D(0.0, 0.0, 0)
        val NOT_DISPLAYABLE: CostedDimension2D = CostedDimension2D(-1.0, -1.0, Long.MAX_VALUE)
        val UNBOUNDED: CostedDimension2D = CostedDimension2D(Double.MAX_VALUE, Double.MAX_VALUE, 0)

        fun calcCost(width: Double, height: Double, within: Dimension2D): Long {
            val extraHeight = max(height - within.height(), 0.0)
            val extraWidth = max(width - within.width(), 0.0)
            val cost =
                    (extraHeight * width + extraWidth * height + extraHeight * extraWidth).toLong()
            return cost
        }

        fun chooseBest(a: CostedDimension2D, b: CostedDimension2D): CostedDimension2D {
            return if (a === NOT_DISPLAYABLE) {
                if (b === NOT_DISPLAYABLE) {
                    NOT_DISPLAYABLE
                } else b
            } else {
                if (a.cost < b.cost) {
                    return a
                } else if (a.cost > b.cost) {
                    return b
                }
                if (a.h < b.h) {
                    return a
                } else if (a.h > b.h) {
                    return b
                }
                if (a.w < b.w) {
                    return a
                } else if (a.w > b.w) {
                    return b
                }
                a
            }
        }
    }

    override fun size(): Dimension2D {
        return this
    }
}
