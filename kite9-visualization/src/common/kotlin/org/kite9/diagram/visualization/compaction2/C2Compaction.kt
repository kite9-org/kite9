package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet

interface C2Compaction {
    fun getSlackOptimisation(d: Dimension): C2SlackOptimisation
    fun getDiagram(): Diagram

    /**
     * Makes sure we know the points in a routable slideable set intersect with a routable slideable set going the
     * other way, and also the rectangular object within it.
     */
    fun setupRectangularBlockers(along: RoutableSlideableSet, perp: RectangularSlideableSet)

    /**
     * When wrapping a routable slideable set in a rectangular (container), we need to make sure that the
     * slideables leaving the routable intersect with the rectangular slideables.
     *
     * We don't need this yet - this will come into play when we have containers that only
     * permit edges to enter from a specific direction
     */
    fun setupContainerBlockers(along: RoutableSlideableSet, inside: RectangularSlideableSet)

    /**
     * This is used when slideables merge
     */
    fun replaceBlockers(s1: C2Slideable?, s2: C2Slideable?, sNew: C2Slideable?)

    val blockers: Map<C2BufferSlideable, Set<C2Slideable>>

    fun consistentBlockers()

    fun checkConsistency()
}