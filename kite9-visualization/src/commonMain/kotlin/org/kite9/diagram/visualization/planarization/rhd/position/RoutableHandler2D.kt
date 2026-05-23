package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * Manages the position of groups in subdividable 2D space
 *
 * @author robmoffat
 */
interface RoutableHandler2D {

    fun getPlacedPosition(r: Group): PositionRoutingInfo

    /**
     * Pretty much the same as the above but for a single dimension.
     * prefer this.
     */
    fun getBoundsOf(r: Group, d: Dimension): Bounds

    fun setPlacedPosition(r: CompoundGroup, d: Direction?)

    /**
     * Works out distance cost of from -> to.
     */
    fun cost(a: Group, b: Group): Double

}