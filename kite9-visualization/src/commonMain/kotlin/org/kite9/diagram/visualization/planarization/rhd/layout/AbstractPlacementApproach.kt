package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LayoutSetPoint
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

abstract class AbstractPlacementApproach(
    val log: Kite9Log,
    val aDirection: Direction?,
    var overall: CompoundGroup,
    override val natural: Boolean,
    val rh: RoutableHandler2D
): PlacementApproach {

    override var score: Double = 0.0

    override fun choose() {
        overall.setLayout(Layout.fromDirection(aDirection), LayoutSetPoint.PLACEMENT_APPROACH)
        rh.setPlacedPosition(overall, aDirection)
        val before = if (aDirection === Direction.LEFT || aDirection === Direction.UP) overall.b else overall.a
        val after = if (aDirection === Direction.LEFT || aDirection === Direction.UP) overall.a else overall.b
        log.send(if (log.go()) null else "Placement $aDirection chosen for --- $before   ---     $after")
    }

}