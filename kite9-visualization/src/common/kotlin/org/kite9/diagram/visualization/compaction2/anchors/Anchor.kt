package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side

/**
 * Maps how a slideable works in relation to the
 * diagram elements around it.
 */
sealed interface Anchor<X> {
    val e: DiagramElement
    val s: X?
}