package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Port
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.LinkNode
import org.kite9.diagram.model.RectangularLinkNode

sealed interface LeafGroup : Group {

    val connected: LinkNode
    val container: Rectangular

    fun sortLink(
        d: Direction?,
        otherGroup: Group,
        linkValue: Float,
        ordering: Boolean,
        linkRank: Int,
        c: Iterable<BiDirectional<Connected>>
    )

    fun getAxisLayout() : Layout? {
        return when (connected) {
            is Port -> null
            is RectangularLinkNode -> {
                return when (val layout = container?.getLayout()) {
                    Layout.HORIZONTAL,
                    Layout.VERTICAL -> layout
                    else -> null
                }
            }
        }
    }
}