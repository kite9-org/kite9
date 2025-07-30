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
    fun setupRectangularIntersections(hr: RectangularSlideableSet, vr: RectangularSlideableSet)

    /**
     * When wrapping a routable slideable set in a rectangular (container or glyph), we need to make sure that the
     * slideables leaving the routable intersect with the rectangular slideables.
     */
    fun propagateIntersectionsRoutableWithRectangular(hi: RoutableSlideableSet, vi: RoutableSlideableSet, ho: RectangularSlideableSet, vo: RectangularSlideableSet)

    /**
     * Used when we create the routable slideable sets, to join their corners.
     */
    fun setupRoutableIntersections(h: RoutableSlideableSet, v: RoutableSlideableSet)

    /**
     * This is used when slideables merge
     */
    fun replaceIntersections(s1: C2Slideable?, s2: C2Slideable?, sNew: C2Slideable?)

    fun getIntersections(s1: C2Slideable) : Set<C2Slideable>?

    fun checkConsistency()
}