package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side

interface Anchor {
    val e: DiagramElement
    val s: Any
}