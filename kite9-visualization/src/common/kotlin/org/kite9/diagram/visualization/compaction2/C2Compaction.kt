package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.compaction2.routing.C2Route
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet

interface C2Compaction {
    fun getSlackOptimisation(d: Dimension): C2SlackOptimisation
    fun getDiagram(): Diagram

//    fun recordJunctions(r1: RectangularSlideableSet, r2: RectangularSlideableSet)
//
    fun replaceJunction(s1: C2Slideable, s2: C2Slideable, sNew: C2Slideable)
}