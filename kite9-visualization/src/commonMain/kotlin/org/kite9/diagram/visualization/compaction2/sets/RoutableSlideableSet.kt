package org.kite9.diagram.visualization.compaction2.sets

import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*

/**
 * A routable slideable set maps to a group, allowing you to move above, below or through the group.
 */
sealed interface RoutableSlideableSet : SlideableSet<RoutableSlideableSet> {

    val c: C2Slideable? // this is only set
    val bl: C2Slideable?
    val br: C2Slideable?

    // the RSS before we replaced the side
    val previous: RoutableSlideableSet?

    override fun getAll() : Set<C2Slideable>


    fun replaceSide(s: C2Slideable?, side: Side) : RoutableSlideableSet
}