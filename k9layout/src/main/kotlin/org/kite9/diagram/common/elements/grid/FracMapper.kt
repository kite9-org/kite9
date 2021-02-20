package org.kite9.diagram.common.elements.grid

import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D
import org.kite9.diagram.common.elements.mapping.BaseGridCornerVertices
import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.common.fraction.LongFraction

interface FracMapper {

    /**
     * Given a laid-out container (i.e. post phase 2 of RHD) this works out, for each fraction used on Grid X/Y positions,
     * where within the bounds of the container's PositionInfo the fractions should be placed.
     * @param containerVertices
     * @param bounds
     */
    fun getFracMapForGrid(
        c: DiagramElement,
        rh: RoutableHandler2D,
        containerVertices: BaseGridCornerVertices,
        bounds: RoutingInfo
    ): OPair<Map<LongFraction, Double>>

    companion object {
        val NULL_FRAC_MAP = FracMapperImpl.createNullFracMap()
    }
}