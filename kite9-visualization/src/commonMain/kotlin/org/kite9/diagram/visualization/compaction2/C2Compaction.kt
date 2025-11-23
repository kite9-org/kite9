package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet

interface C2Compaction {

    fun getSlackOptimisation(d: Dimension): C2SlackOptimisation
    fun getDiagram(): Diagram

    fun buildNeighbours()

    /**
     * Places a neighbour on the along slideable, allowing routing from n1 to n2 or vice versa.
     */
    fun addNeighbour(along: C2Slideable?, n1: C2Slideable?, n2: C2Slideable?)

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