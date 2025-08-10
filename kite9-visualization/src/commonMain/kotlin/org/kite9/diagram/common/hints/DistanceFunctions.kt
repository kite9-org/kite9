package org.kite9.diagram.common.hints

import org.kite9.diagram.common.objects.BasicBounds
import org.kite9.diagram.common.objects.Bounds

/**
 * Standalone distance calculation functions to replace deprecated PositioningHints functions. These
 * functions provide the same functionality without using the deprecated object.
 */
fun planarizationDistance(a: Map<String, Float?>, b: Map<String, Float?>): Float? {
    val xD = scalarDistance(a, b, "px1", "px2")
    val yD = scalarDistance(a, b, "py1", "py2")
    return if (xD == null || yD == null) {
        null
    } else xD + yD
}

fun positionDistance(a: Map<String, Float?>, b: Map<String, Float?>): Float? {
    val xD = scalarDistance(a, b, "x1", "x2")
    val yD = scalarDistance(a, b, "y1", "y2")
    return if (xD == null || yD == null) {
        null
    } else xD + yD
}

private fun scalarDistance(
        a: Map<String, Float?>,
        b: Map<String, Float?>,
        b1: String,
        b2: String
): Float? {
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

fun compareEitherXBounds(from: Map<String, Float?>, to: Map<String, Float?>): Int? {
    return compareEitherBounds(from, to, "px1", "px2", "x1", "x2")
}

fun compareEitherYBounds(from: Map<String, Float?>, to: Map<String, Float?>): Int? {
    return compareEitherBounds(from, to, "py1", "py2", "y1", "y2")
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

private fun compareBounds(
        from: Map<String, Float?>,
        to: Map<String, Float?>,
        p1: String,
        p2: String
): Int? {
    val fb = createBounds(from, p1, p2)
    val tb = createBounds(to, p1, p2)
    return if (fb == null || tb == null) {
        null
    } else {
        fb.compareTo(tb)
    }
}
