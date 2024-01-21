package org.kite9.diagram.visualization.compaction2.sets;

import org.kite9.diagram.visualization.compaction2.C2IntersectionSlideable
import org.kite9.diagram.visualization.compaction2.C2OrbitSlideable
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.compaction2.sets.SlideableSet

/**
 * A routable slideable set maps to a group, allowing you to move above, below or through the group.
 */
interface RoutableSlideableSet : SlideableSet<RoutableSlideableSet> {

    val c: C2IntersectionSlideable?
    val bl: C2OrbitSlideable
    val br: C2OrbitSlideable

    fun replaceOrbit(s: C2OrbitSlideable, with: C2OrbitSlideable) : RoutableSlideableSet

    override fun replaceGeneric(s: C2Slideable, with: C2Slideable) : RoutableSlideableSet {
        println("Replacing $s with $with" )
        return if ((s is C2OrbitSlideable) && (with is C2OrbitSlideable)) {
            replaceOrbit(s, with)
        } else {
            super.replaceGeneric(s, with)
        }
    }

}