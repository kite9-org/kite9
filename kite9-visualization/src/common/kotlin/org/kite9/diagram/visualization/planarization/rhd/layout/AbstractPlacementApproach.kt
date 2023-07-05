package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

abstract class AbstractPlacementApproach(
    val log: Kite9Log,
    val aDirection: Layout?,
    var overall: CompoundGroup,
    val rh: RoutableHandler2D,
    val setHoriz: Boolean,
    val setVert: Boolean,
    override val natural: Boolean
): PlacementApproach {

    override var score: Double = 0.0

    override fun choose() {
        overall.layout = aDirection
        val before = if (aDirection === Layout.LEFT || aDirection === Layout.UP) overall.b else overall.a
        val after = if (aDirection === Layout.LEFT || aDirection === Layout.UP) overall.a else overall.b
        log.send(if (log.go()) null else "Placement $aDirection chosen for --- $before   ---     $after")
    }

}