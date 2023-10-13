package org.kite9.diagram.visualization.planarization.rhd.layout

import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

class DirectionLayoutStrategy(rh: RoutableHandler2D) : AbstractTopDownLayoutStrategy(rh) {

    public override fun createPlacementApproach(
        gg: CompoundGroup, ld: Layout?,
        setHoriz: Boolean, setVert: Boolean, natural: Boolean
    ): PlacementApproach {
        return DirectionPlacementApproach(log, ld, gg, rh, natural)
    }
}