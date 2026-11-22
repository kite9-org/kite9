package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group

import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager

enum class LayoutSetPoint {
    ON_CREATION, AXIS_FORCED, PLACEMENT_APPROACH
}

sealed interface CompoundGroup : Group {

    val a: Group
    val b: Group

    val internalLinkA: LinkManager.LinkDetail?

    val internalLinkB: LinkManager.LinkDetail?

    fun setLayout(l: Layout?, t: LayoutSetPoint)

    /**
     * When the layout was set
     */
    fun getLayoutSetPoint() : LayoutSetPoint?
}