package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.compaction2.routing.C2Route
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet

interface C2Compaction {
    fun getSlackOptimisation(d: Dimension): C2SlackOptimisation
    fun getDiagram(): Diagram

    /**
     * Makes sure we know thw points in a routable slideable set intersect with a routable slideable set going the
     * other way, and also the rectangular object within it.
     */
    fun createInitialJunctions(along: RoutableSlideableSet, r1: RoutableSlideableSet, r2: RectangularSlideableSet?)

    /**
     * When wrapping a routable slideable set in a rectangular (container), we need to make sure that the
     * slideables leaving the routable intersect with the rectangular slideables.
     */
    fun createContainerJunctions(along: RectangularSlideableSet, inside: RoutableSlideableSet)

    /**
     * When wrapping a container inside a routable, we need to make sure that the
     * slideables leaving the container intersect with the routable slideables.
     */
    fun createRoutableJunctions(along: RoutableSlideableSet, r1: RoutableSlideableSet)

    /**
     * This is used when slideables merge
     */
    fun replaceJunction(s1: C2Slideable?, s2: C2Slideable?, sNew: C2Slideable?)

    val junctions: Map<C2BufferSlideable, List<C2Slideable>>

    fun resortJunctions()
    fun consistentJunctions()

    fun checkConsistency()
}