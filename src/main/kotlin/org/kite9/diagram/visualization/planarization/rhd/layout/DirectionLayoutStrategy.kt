package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

class DirectionLayoutStrategy(rh: RoutableHandler2D) : AbstractTopDownLayoutStrategy(rh) {

    public override fun createPlacementApproach(
        gp: GroupPhase, gg: CompoundGroup, ld: Layout?,
        setHoriz: Boolean, setVert: Boolean, natural: Boolean
    ): PlacementApproach {
        return DirectionPlacementApproach(log, gp, ld, gg, rh, setHoriz, setVert, natural)
    }
}