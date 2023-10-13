package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement

interface C2Compaction {
    fun getSlackOptimisation(d: Dimension): C2SlackOptimisation
    fun getDiagram(): Diagram
    fun getSegments(de: DiagramElement, d: Dimension) : SlideableSet? {
        return getSlackOptimisation(d).getSlideablesFor(de)
    }

}