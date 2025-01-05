package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.model.Connection
import org.kite9.diagram.visualization.compaction2.anchors.Anchor

/**
 * Anchor for a point in a connection, numbered from zero
 *
 */
data class ConnAnchor(override val e: Connection, override val s: Float, val terminal: Boolean) : Anchor<Float> {
}