package org.kite9.diagram.common.objects

enum class DPos {
    BEFORE, OVERLAP, AFTER
}

interface Bounds : Comparable<Bounds> {

    val distanceMin: Double
    val distanceMax: Double
    val distanceCenter: Double

    /**
     * Returns a new bounds large enough to take both this and other.
     */
    fun expand(other: Bounds): Bounds

    fun compareBounds(other: Bounds): DPos
}