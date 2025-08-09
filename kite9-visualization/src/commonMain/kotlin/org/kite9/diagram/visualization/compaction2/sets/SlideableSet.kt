package org.kite9.diagram.visualization.compaction2.sets

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.visualization.compaction2.*

interface SlideableSet<X : SlideableSet<X>> {
    /**
     * Returns the collection of slideables that enter one side
     * of the set and leave on the other.
     */
    fun getAll() : Set<C2Slideable>

    fun replace(s: C2Slideable, with: C2Slideable) : X

    val done : Boolean

    val number : Int
}