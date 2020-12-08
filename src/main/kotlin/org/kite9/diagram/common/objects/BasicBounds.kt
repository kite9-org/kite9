package org.kite9.diagram.common.objects

import org.kite9.diagram.common.fraction.BigFraction
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
            Math.min(o2.distanceMin, distanceMin),
            Math.max(o2.distanceMax, distanceMax)
        )
    }

    override fun narrow(other: Bounds): Bounds {
        if (this === EMPTY_BOUNDS) {
            return this
        } else if (other === EMPTY_BOUNDS) {
            return other
        }
        val o2 = other as BasicBounds
        val lower = Math.max(o2.distanceMin, distanceMin)
        val upper = Math.min(o2.distanceMax, distanceMax)
        return if (lower >= upper) {
            EMPTY_BOUNDS
        } else BasicBounds(lower, upper)
    }

    override val distanceCenter: Double
        get() = (distanceMax + distanceMin) / 2.0

    override fun toString(): String {
        return "(bb, g=%.2f - %.2f)".format(distanceMin, distanceMax);
    }

    /**
     * As well as following the usual -1, 0, 1 compare operation, this also returns the common level of the two bounds
     * being compared.
     */
    override operator fun compareTo(s: Bounds): Int {
        return if (distanceMax < s.distanceMin) {
            -1
        } else if (distanceMin > s.distanceMax) {
            1
        } else {
            0
        }
    }

    override fun keep(buffer: Double, width: Double, atFraction: BigFraction): Bounds {
        val span = distanceMax - distanceMin - buffer * 2.0
        val pos = atFraction.doubleValue() * span
        var lower = distanceMin + pos - width / 2.0 + buffer
        var upper = distanceMin + pos + width / 2.0 + buffer
        lower = Math.max(distanceMin + buffer, lower)
        upper = Math.min(distanceMax - buffer, upper)
        return BasicBounds(lower, upper)
    }

    override fun keep(buffer: Double, width: Double, fraction: Double): Bounds {
        val span = distanceMax - distanceMin - buffer * 2.0
        val pos = fraction * span
        var lower = distanceMin + pos - width / 2.0 + buffer
        var upper = distanceMin + pos + width / 2.0 + buffer
        lower = Math.max(distanceMin + buffer, lower)
        upper = Math.min(distanceMax - buffer, upper)
        return BasicBounds(lower, upper)
    }

    override fun narrow(vertexTrim: Double): Bounds {
        return BasicBounds(distanceMin + vertexTrim, distanceMax - vertexTrim)
    }

    companion object {
        @JvmField
		val EMPTY_BOUNDS = BasicBounds(-1.0, -1.0)
    }

    init {
        if (distanceMin > distanceMax) {
            throw LogicException("Illegal Bounds")
        }
    }
}