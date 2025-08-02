package org.kite9.diagram.visualization.compaction2.sets

import org.kite9.diagram.visualization.compaction2.*

/**
 * A routable slideable set maps to a group, allowing you to move above, below or through the group.
 */
interface RoutableSlideableSet : SlideableSet<RoutableSlideableSet> {

    val c: Set<C2Slideable>
    val bl: C2Slideable?
    val br: C2Slideable?

    override fun getAll() : Set<C2Slideable>

    fun mergeWithOverlap(over: RoutableSlideableSet, c2: C2SlackOptimisation, alignIntersects: Boolean) : RoutableSlideableSet

    fun mergeWithGutter(after: RoutableSlideableSet, c2: C2SlackOptimisation) : RoutableSlideableSet

    fun getBufferSlideables() : Set<C2Slideable>
}