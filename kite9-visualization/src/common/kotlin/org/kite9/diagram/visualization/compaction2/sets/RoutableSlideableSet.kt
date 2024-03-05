package org.kite9.diagram.visualization.compaction2.sets

import org.kite9.diagram.visualization.compaction2.*

/**
 * A routable slideable set maps to a group, allowing you to move above, below or through the group.
 */
interface RoutableSlideableSet : SlideableSet<RoutableSlideableSet> {

    val c: Set<C2BufferSlideable>
    val bl: C2OrbitSlideable?
    val br: C2OrbitSlideable?

    override fun getAll() : Set<C2BufferSlideable>


    fun replaceOrbit(s: C2OrbitSlideable, with: C2OrbitSlideable) : RoutableSlideableSet

    override fun replaceGeneric(s: C2Slideable, with: C2Slideable) : RoutableSlideableSet {
        println("Replacing $s with $with" )
        return if ((s is C2OrbitSlideable) && (with is C2OrbitSlideable)) {
            replaceOrbit(s, with)
        } else if ((s is C2IntersectionSlideable) && (with is C2IntersectionSlideable)) {
            replaceIntersection(s, with)
        } else {
            super.replaceGeneric(s, with)
        }
    }

    fun mergeWithOverlap(over: RoutableSlideableSet, c2: C2SlackOptimisation) : RoutableSlideableSet

    fun mergeWithGutter(after: RoutableSlideableSet, c2: C2SlackOptimisation) : RoutableSlideableSet

    fun replaceIntersection(s: C2IntersectionSlideable, with: C2IntersectionSlideable) : RoutableSlideableSet

    fun getBufferSlideables() : Set<C2BufferSlideable>
}