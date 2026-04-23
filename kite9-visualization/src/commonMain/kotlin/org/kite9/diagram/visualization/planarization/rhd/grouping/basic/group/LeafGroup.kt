package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupLinkNode

sealed interface LeafGroup : Group {

    val connected: GroupLinkNode
    val container: Rectangular

    fun sortLink(
        d: Direction?,
        otherGroup: Group,
        linkValue: Float,
        ordering: Boolean,
        linkRank: Int,
        c: Iterable<BiDirectional<Connected>>
    )

    companion object {

        fun getAxisLayout(container: Rectangular?) : Layout? {
            val layout = container?.getLayout()
            return when (layout) {
                Layout.HORIZONTAL,
                Layout.VERTICAL -> layout
                else -> null
            }
        }

    }
}