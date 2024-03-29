package org.kite9.diagram.common.objects

import org.kite9.diagram.common.fraction.LongFraction

interface Bounds : Comparable<Bounds> {
    val distanceMin: Double
    val distanceMax: Double
    val distanceCenter: Double

    /**
     * Returns a new bounds large enough to take both this and other.
     */
    fun expand(other: Bounds): Bounds

    /**
     * Returns a new bounds with just the common distance inside
     */
    fun narrow(other: Bounds): Bounds
    fun keep(buffer: Double, width: Double, atFraction: LongFraction): Bounds
    fun keep(buffer: Double, width: Double, atFraction: Double): Bounds
    fun narrow(trim: Double): Bounds

    fun size() : Double

}