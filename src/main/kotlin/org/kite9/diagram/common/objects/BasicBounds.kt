package org.kite9.diagram.common.objects

import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.common.objects.BasicBounds
import org.apache.commons.math.fraction.BigFraction
import java.text.NumberFormat
import java.text.DecimalFormat
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
        return "(bb, g=" +
                nf.format(distanceMin) +
                "-" +
                nf.format(distanceMax) + ")"
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
        val pos = atFraction.toDouble() * span
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

    //	@Override
    //	public Bounds keepMax(double lb, double ub) {
    //		if ((lb == 0) && (ub == 0)) {
    //			return this;
    //		}
    //		return new BasicBounds(this.max - ub, this.max - lb);
    //	}
    //
    //
    //	@Override
    //	public Bounds keepMin(double lb, double ub) {
    //		if ((lb == 0) && (ub == 0)) {
    //			return this;
    //		}
    //		return new BasicBounds(this.min+lb ,this.min+ub);
    //	}
    //	
    //
    //	@Override
    //	public Bounds keepMid(double w) {
    //		double mid = (this.min + this.max) / 2d;
    //		if ((mid <0) || (mid > 1)) {
    //			return BasicBounds.EMPTY_BOUNDS;
    //		}
    //		return new BasicBounds(mid-(w/2),mid+(w/2));
    //	}
    override fun narrow(vertexTrim: Double): Bounds {
        return BasicBounds(distanceMin + vertexTrim, distanceMax - vertexTrim)
    }

    companion object {
        @JvmField
		val EMPTY_BOUNDS = BasicBounds(-1.0, -1.0)
        val nf: NumberFormat = DecimalFormat(".0000")
    }

    init {
        if (distanceMin > distanceMax) {
            throw LogicException("Illegal Bounds")
        }
    }
}