package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side

/**
 * Anchor for a point in a connection, numbered from zero
 */
data class ConnAnchor(override val e: Connection, override val s: Int) : Anchor {
}