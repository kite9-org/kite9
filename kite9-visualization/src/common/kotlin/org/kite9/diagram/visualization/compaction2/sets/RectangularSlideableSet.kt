package org.kite9.diagram.visualization.compaction2.sets

import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

interface RectangularSlideableSet : SlideableSet<RectangularSlideableSet> {

    val d: Rectangular
    val l: C2RectangularSlideable
    val r: C2RectangularSlideable

    fun getRectangularSlideables(): Collection<C2RectangularSlideable>

    fun wrapInRoutable(so: C2SlackOptimisation, g: LeafGroup?) : RoutableSlideableSet


}