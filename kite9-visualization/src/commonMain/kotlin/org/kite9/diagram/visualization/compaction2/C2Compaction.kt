package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet

interface C2Compaction {

    fun getSlackOptimisation(d: Dimension): C2SlackOptimisation
    fun getDiagram(): Diagram

    /**
     * Around a rectangular (e.g. container or glyph) the intersection slideables of one dimension should meet
     * the rectangular slideables of the other.
     */
    fun setupRectangularIntersections(hr: RectangularSlideableSet, vr: RectangularSlideableSet, ho: RoutableSlideableSet, vo: RoutableSlideableSet)

    /**
     * When wrapping a routable slideable set in a rectangular (container or glyph), we need to make sure that the
     * 1) slideables leaving the routable intersect with the rectangular slideables.
     * 2) intersections on the rectangular slideable are propagated to the inner slideables.
     */
    fun propagateIntersectionsBetweenRoutableAndOuterRectangular(
        hi: RoutableSlideableSet,
        vi: RoutableSlideableSet,
        ho: RectangularSlideableSet,
        vo: RectangularSlideableSet
    )

    /**
     * When wrapping a rectangular slideable set in its routable, ensure that intersections are propagated.
     */
    fun propagateIntersectionsFromRectangularToOuterRoutable(hi: RectangularSlideableSet, vi: RectangularSlideableSet, ho: RoutableSlideableSet, vo: RoutableSlideableSet)

    /**
     * Used when we create the routable slideable sets, anything meeting an orbit slideable should form an intersection.
     */
    fun setupRoutableIntersections(h: RoutableSlideableSet, v: RoutableSlideableSet)

    /**
     * This is used when slideables merge (in C2SlackOptimisation)
     */
    fun replaceIntersections(s1: C2Slideable, s2: C2Slideable, sNew: C2Slideable)

    /**
     * Used to retrieve intersections when we're doing routing.
     */
    fun getNeighbours(along: C2Slideable, perp: C2Slideable) : Set<C2Slideable>

    fun getNeighbourSetsOn(s: C2Slideable) : Set<Set<C2Slideable>>

    /**
     * Like the above but with less neighbour information.
     */
    fun getLocationsOn(s: C2Slideable) : Set<C2Slideable>

    fun checkConsistency()

    fun joinOverlappingNeighbourGroups()
}