package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side


/**
 * Anchor for part of a rectangular in a slideable, e.g. top, bottom etc.
 */
data class RectAnchor(override val e: Rectangular, override val s: Side) : Anchor