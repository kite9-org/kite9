package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side


enum class AnchorType {
    REGULAR,
    PRE_FAN,
    AFTER_FAN,
    TERMINAL
}
/**
 * Anchor for a point in a connection, numbered from zero
 *
 */
data class ConnAnchor(
    override val e: Connection,
    override val s: Float,
    val type: AnchorType,
    val connectedSide: Side,
    val connectedEnd : Connected?,
    val comingFrom: Direction?,
    val goingTo: Direction?
) : Anchor<Float> {
}