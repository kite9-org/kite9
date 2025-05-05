package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.model.Connected

/**
 * This keeps track of the ordering pf fanning connections around the connected that
 * they all meet.
 */
data class FanAnchor(override val e: Connected, override val s: Int) : Anchor<Int> {
}