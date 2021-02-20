package org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.position.Direction

interface LeafGroup : Group {

    val contained: Connected?

    val container: Container?

    fun sortLink(
        d: Direction?,
        otherGroup: Group,
        linkValue: Float,
        ordering: Boolean,
        linkRank: Int,
        c: Iterable<BiDirectional<Connected>>
    )
}