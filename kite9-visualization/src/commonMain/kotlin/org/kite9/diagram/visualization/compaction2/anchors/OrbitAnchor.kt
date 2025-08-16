package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side

/**
 * Anchor to say that the slideable passes to the side of this element.
 */
data class OrbitAnchor(override val e: Rectangular, override val s: Side?) : Anchor<Side>