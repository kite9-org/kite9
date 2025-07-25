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
    fun setupLeafRectangularIntersections(ho: RoutableSlideableSet, vo: RoutableSlideableSet, hi: RectangularSlideableSet, vi: RectangularSlideableSet)

    /**
     * Similar to the above, but this does the same thing for containers.  The intersections of the container must meet the edges of the container
     * in the other dimension.
     */
    fun setupContainerRectangularIntersections(hr: RectangularSlideableSet, vr: RectangularSlideableSet)


    /**
     * This is used when wrapping containers into a RoutableSlideableSet.  The container's leavers
     * must intersect with the RoutableSlideableSet
     */
    //fun setupContainerIntersections(along: RoutableSlideableSet, inside: RectangularSlideableSet)

    /**
     * When wrapping a routable slideable set in a rectangular (container), we need to make sure that the
     * slideables leaving the routable intersect with the rectangular slideables.
     */
    fun propagateIntersectionsRoutableWithRectangular(hi: RoutableSlideableSet, vi: RoutableSlideableSet, ho: RectangularSlideableSet, vo: RectangularSlideableSet)

    /**
     * Used when we create the routable slideable sets, to join their corners.
     */
    fun setupRoutableIntersections(a: RoutableSlideableSet, b: RoutableSlideableSet)

    fun setupRoutableCorners(a: RoutableSlideableSet, b: RoutableSlideableSet)
    /**
     * This is used when slideables merge
     */
    fun replaceIntersections(s1: C2Slideable?, s2: C2Slideable?, sNew: C2Slideable?)

    fun setIntersection(s1: C2Slideable, s2: C2Slideable)

    fun getIntersections(s1: C2Slideable) : Set<C2Slideable>?

    fun checkConsistency()
}