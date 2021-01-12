package org.kite9.diagram.common.hints

import org.kite9.diagram.common.objects.BasicBounds
import org.kite9.diagram.common.objects.Bounds

/**
 * Hints are attached to [PositionableDiagramElement]s.  These allow the grouping and layout
 * to proceed along lines already established by a previous rendering.
 *
 * @author robmoffat
 */
@Deprecated("")
object PositioningHints {
    const val PLAN_MIN_X = "px1"
    const val PLAN_MAX_X = "px2"
    const val PLAN_MIN_Y = "py1"
    const val PLAN_MAX_Y = "py2"
    const val MIN_X = "x1"
    const val MAX_X = "x2"
    const val MIN_Y = "y1"
    const val MAX_Y = "y2"
    var PLANARIZATION_HINTS: MutableMap<String, Approach> = HashMap()
    var POSITION_HINTS: MutableMap<String, Approach> = HashMap()
    var ALL_HINTS: MutableMap<String, Approach> = HashMap()
    @JvmStatic

	fun merge(a: Map<String, Float?>, b: Map<String, Float?>): Map<String, Float?> {
        if (a.size == 0 && b.size == 0) {
            return emptyMap()
        }
        val out: MutableMap<String, Float?> = HashMap()
        for (k : String  in ALL_HINTS.keys) {
            val v : Approach = ALL_HINTS[k]!!
            out[k] = v.merge(a[k], b[k])
        }
        return out
    }

    fun writeHints(from: Map<String?, Float?>, to: MutableMap<String?, Float?>, hints: Map<String?, Approach?>) {
        for (h in hints.keys) {
            val v = from[h]
            if (v != null) {
                to[h] = v
            }
        }
    }

    @JvmStatic
	fun compareEitherXBounds(from: Map<String, Float?>, to: Map<String, Float?>): Int? {
        return compareEitherBounds(from, to, PLAN_MIN_X, PLAN_MAX_X, MIN_X, MAX_X)
    }

    @JvmStatic
	fun compareEitherYBounds(from: Map<String, Float?>, to: Map<String, Float?>): Int? {
        return compareEitherBounds(from, to, PLAN_MIN_Y, PLAN_MAX_Y, MIN_Y, MAX_Y)
    }

    private fun compareEitherBounds(
        from: Map<String, Float?>,
        to: Map<String, Float?>,
        p1: String,
        p2: String,
        a1: String,
        a2: String
    ): Int? {
        var bc = compareBounds(from, to, p1, p2)
        if (bc == null || bc == 0) {
            bc = compareBounds(from, to, a1, a2)
        }
        return bc
    }

    private fun compareBounds(from: Map<String, Float?>, to: Map<String, Float?>, p1: String, p2: String): Int? {
        val fb = createBounds(from, p1, p2)
        val tb = createBounds(to, p1, p2)
        return if (fb == null || tb == null) {
            null
        } else {
            fb.compareTo(tb)
        }
    }

    @JvmStatic
	fun planarizationDistance(a: Map<String, Float?>, b: Map<String, Float?>): Float? {
        val xD = scalarDistance(a, b, "px1", "px2")
        val yD = scalarDistance(a, b, "py1", "py2")
        return if (xD == null || yD == null) {
            null
        } else xD + yD
    }

    @JvmStatic
	fun positionDistance(a: Map<String, Float?>, b: Map<String, Float?>): Float? {
        val xD = scalarDistance(a, b, "x1", "x2")
        val yD = scalarDistance(a, b, "y1", "y2")
        return if (xD == null || yD == null) {
            null
        } else xD + yD
    }

    fun scalarDistance(a: Map<String, Float?>, b: Map<String, Float?>, b1: String, b2: String): Float? {
        val aBounds = createBounds(a, b1, b2)
        val bBounds = createBounds(b, b1, b2)
        return boundsDistance(aBounds, bBounds)
    }

    private fun boundsDistance(aBounds: Bounds?, bBounds: Bounds?): Float? {
        if (aBounds == null || bBounds == null) {
            return null
        }
        return if (aBounds.distanceMax < bBounds.distanceMin) {
            (bBounds.distanceMin - aBounds.distanceMax).toFloat()
        } else if (aBounds.distanceMin > bBounds.distanceMax) {
            (aBounds.distanceMin - bBounds.distanceMax).toFloat()
        } else {
            0f
        }
    }

    private fun createBounds(a: Map<String, Float?>, b1: String, b2: String?): Bounds? {
        val min = a[b1]
        val max = a[b2]
        return if (min == null || max == null) {
            null
        } else BasicBounds(min.toDouble(), max.toDouble())
    }

    fun planFill(map: MutableMap<String?, Float?>, px1: Float, px2: Float, py1: Float, py2: Float) {
        map[PLAN_MIN_X] = px1
        map[PLAN_MIN_Y] = py1
        map[PLAN_MAX_X] = px2
        map[PLAN_MAX_Y] = py2
    }

    enum class Approach {
        MAX, MIN;

        fun merge(a: Float?, b: Float?): Float? {
            if (a == null) {
                return b
            } else if (b == null) {
                return a
            } else if (this == MAX) {
                return Math.max(a, b)
            } else if (this == MIN) {
                return Math.min(a, b)
            }
            return null
        }
    }

    init {
        PLANARIZATION_HINTS[PLAN_MIN_X] = Approach.MIN
        PLANARIZATION_HINTS[PLAN_MIN_Y] = Approach.MIN
        PLANARIZATION_HINTS[PLAN_MAX_X] = Approach.MAX
        PLANARIZATION_HINTS[PLAN_MAX_Y] = Approach.MAX
        POSITION_HINTS[MIN_X] = Approach.MIN
        POSITION_HINTS[MIN_Y] = Approach.MIN
        POSITION_HINTS[MAX_X] = Approach.MAX
        POSITION_HINTS[MAX_Y] = Approach.MAX
        ALL_HINTS.putAll(PLANARIZATION_HINTS)
        ALL_HINTS.putAll(POSITION_HINTS)
    }
}