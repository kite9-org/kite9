package org.kite9.diagram.common.objects

import org.kite9.diagram.common.fraction.LongFraction
import org.kite9.diagram.logging.LogicException

data class BasicBounds(override val distanceMin: Double, override val distanceMax: Double) : Bounds {

    override fun expand(other: Bounds): Bounds {
        if (this === EMPTY_BOUNDS) {
            return other
        } else if (other === EMPTY_BOUNDS) {
            return this
        }
        val o2 = other as BasicBounds
        //System.out.println("merging "+o2+" with "+this);
        return BasicBounds(
            o2.distanceMin.coerceAtMost(distanceMin),
            o2.distanceMax.coerceAtLeast(distanceMax)
        )
    }

    override fun narrow(other: Bounds): Bounds {
        if (this === EMPTY_BOUNDS) {
            return this
        } else if (other === EMPTY_BOUNDS) {
            return other
        }
        val o2 = other as BasicBounds
        val lower = o2.distanceMin.coerceAtLeast(distanceMin)
        val upper = o2.distanceMax.coerceAtMost(distanceMax)
        return if (lower >= upper) {
            EMPTY_BOUNDS
        } else BasicBounds(lower, upper)
    }

    override val distanceCenter: Double
        get() = (distanceMax + distanceMin) / 2.0

    override fun toString(): String {
        return "(bb, g=$distanceMin%.2f - $distanceMax%.2f)"
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

    override fun keep(buffer: Double, width: Double, atFraction: LongFraction): Bounds {
        val span = distanceMax - distanceMin - buffer * 2.0
        val pos = atFraction.doubleValue() * span
        var lower = distanceMin + pos - width / 2.0 + buffer
        var upper = distanceMin + pos + width / 2.0 + buffer
        lower = (distanceMin + buffer).coerceAtLeast(lower)
        upper = (distanceMax - buffer).coerceAtMost(upper)
        return BasicBounds(lower, upper)
    }

    override fun keep(buffer: Double, width: Double, atFraction: Double): Bounds {
        val span = (distanceMax - distanceMin - buffer * 2.0).coerceAtLeast(0.0)
        val pos = atFraction * span
        var lower = distanceMin + pos - width / 2.0 + buffer
        var upper = distanceMin + pos + width / 2.0 + buffer
        lower = (distanceMin + buffer).coerceAtLeast(lower).coerceAtMost(upper)
        upper = (distanceMax - buffer).coerceAtMost(upper).coerceAtLeast(lower)
        return BasicBounds(lower, upper)
    }

    override fun size(): Double {
        return distanceMax - distanceMin
    }

    override fun narrow(trim: Double): Bounds {
        return BasicBounds(distanceMin + trim, distanceMax - trim)
    }

    companion object {

        val EMPTY_BOUNDS = BasicBounds(-1.0, -1.0)
    }

    init {
        if ((distanceMin == -1.0) && (distanceMax == -1.0)) {
            // empty bounds, fine
        } else if ((distanceMin < 0) || (distanceMin > 1)) {
            throw LogicException("Illegal Bounds")
        } else if ((distanceMax < 0) || (distanceMax > 1)) {
            throw LogicException("Illegal Bounds")
        } else if (distanceMin > distanceMax) {
            throw LogicException("Illegal Bounds")
        }
    }
}