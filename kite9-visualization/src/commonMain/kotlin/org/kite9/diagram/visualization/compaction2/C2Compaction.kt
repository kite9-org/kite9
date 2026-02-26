package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Diagram

interface C2Compaction {

    fun getSlackOptimisation(d: Dimension): C2SlackOptimisation
    fun getDiagram(): Diagram

    /**
     * Sets up allowed intersections
     */
    fun addNeighbour(along: C2Slideable?, n1: C2Slideable?, n2: C2Slideable?)

    /**
     * Used to retrieve intersections when we're doing routing.
     */
    fun getNeighbours(along: C2Slideable, perp: C2Slideable) : Set<C2Slideable>

    /**
     * This makes sure that neighbours that arrive at a slideable can also leave
     */
    fun invertNeighbours(perp: C2Slideable?)

    fun getNeighbourSetsOn(s: C2Slideable) : Set<Set<C2Slideable>>

    /**
     * Like the above but with less neighbour information.
     */
    fun getLocationsOn(s: C2Slideable) : Set<C2Slideable>

    fun checkConsistency()

    fun joinOverlappingNeighbourGroups()

    /**
     * Duplicates neighbours arriving on one slideable to the other
     */
    fun copyNeighbourMap(from: C2Slideable?, to: C2Slideable?)
}