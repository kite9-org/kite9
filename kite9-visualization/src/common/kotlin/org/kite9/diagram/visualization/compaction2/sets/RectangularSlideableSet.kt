package org.kite9.diagram.visualization.compaction2.sets

import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

interface RectangularSlideableSet : SlideableSet<RectangularSlideableSet> {

    val d: Rectangular
    val l: C2Slideable
    val r: C2Slideable

    fun getRectangularSlideables(): Collection<C2Slideable>

    fun wrapInRoutable(so: C2SlackOptimisation, g: LeafGroup?) : RoutableSlideableSet


}