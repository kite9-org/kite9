package org.kite9.diagram.visualization.compaction2.sets;

import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.sets.SlideableSet

interface RectangularSlideableSet : SlideableSet<RectangularSlideableSet> {

    val d: Rectangular
    val l: C2RectangularSlideable
    val r: C2RectangularSlideable
    val c: C2IntersectionSlideable?

    fun getRectangularSlideables(): Collection<C2RectangularSlideable>

    override fun getBufferSlideables() : Set<C2BufferSlideable> = setOfNotNull(c)

    fun wrapInRoutable(so: C2SlackOptimisation) : RoutableSlideableSet



}