package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.objects.BasicBounds
import org.kite9.diagram.common.objects.DPos
import org.kite9.diagram.common.objects.Division
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis

/**
 * Implementation of the [RoutableHandler2D] functionality, but using 2D RoutableHandler Bounds as the
 * underlying storage.
 *
 * @author robmoffat
 */
class PositionRoutableHandler2D : AbstractPositionRoutableReader(), RoutableHandler2D, Logable {

    val placedX: MutableMap<Group, BasicBounds> = HashMap(1000)
    val placedY: MutableMap<Group, BasicBounds> = HashMap(1000)
    val decidedPositionsX: MutableMap<Group, Division> = HashMap(1000)
    val decidedPositionsY: MutableMap<Group, Division> = HashMap(1000)

    var log = Kite9Log.instance(this)

    override fun getPlacedPosition(r: Group): PositionRoutingInfo {
        return createRouting(getBoundsOf(r, Dimension.H), getBoundsOf(r, Dimension.V))
    }

    override fun getBoundsOf(r: Group, d: Dimension): BasicBounds {
        val out =  when (d) {
            Dimension.H -> placedX.getOrPut(r) { calculateSubBounds(r, d) }
            Dimension.V -> placedY.getOrPut(r) { calculateSubBounds(r, d) }
        }

        return out
    }

    override fun setPlacedPosition(
        r: CompoundGroup,
        d: Direction?
    ) {
        // remove old values
        decidedPositionsX.remove(r.a)
        decidedPositionsX.remove(r.b)
        decidedPositionsY.remove(r.a)
        decidedPositionsY.remove(r.b)

        when (d) {
            Direction.UP -> {
                decidedPositionsY[r.a] = Division.SECOND_HALF
                decidedPositionsY[r.b] = Division.FIRST_HALF
            }
            Direction.DOWN -> {
                decidedPositionsY[r.a] = Division.FIRST_HALF
                decidedPositionsY[r.b] = Division.SECOND_HALF
            }
            Direction.LEFT -> {
                decidedPositionsX[r.a] = Division.SECOND_HALF
                decidedPositionsX[r.b] = Division.FIRST_HALF
            }
            Direction.RIGHT -> {
                decidedPositionsX[r.a] = Division.FIRST_HALF
                decidedPositionsX[r.b] = Division.SECOND_HALF
            }
            null -> {
                // do nothing.
            }
        }

        clearSelfAndChildrenFromCache(r.a)
        clearSelfAndChildrenFromCache(r.b)
    }



    private fun calculateSubBounds(r: Group, d: Dimension) : BasicBounds {
        return when (d) {
            Dimension.H -> {
                val parent = (r.axis as DirectedGroupAxis).vAxisParentGroup
                if (parent == null) {
                    // top level
                    return BasicBounds()
                } else {
                    val pBounds = getBoundsOf(parent, Dimension.H)
                    val pp = decidedPositionsX[r]
                    BasicBounds.extendBounds(pBounds, pp)
                }
            }
            Dimension.V -> {
                val parent = (r.axis as DirectedGroupAxis).hAxisParentGroup
                if (parent == null) {
                    return BasicBounds()
                } else {
                    val pBounds = getBoundsOf(parent, Dimension.V)
                    val pp = decidedPositionsY[r]
                    BasicBounds.extendBounds(pBounds, pp)
                }
            }
        }
    }

    private fun clearSelfAndChildrenFromCache(r: Group) {
        placedX.remove(r)
        placedY.remove(r)
        if (r is CompoundGroup) {
            clearSelfAndChildrenFromCache(r.a)
            clearSelfAndChildrenFromCache(r.b)
        }
    }

    fun createRouting(x: BasicBounds, y: BasicBounds): PositionRoutingInfo {
        return BoundsBasedPositionRoutingInfo(x, y)
    }

    override val prefix: String
        get() = "RH2D"
    override val isLoggingEnabled: Boolean
        get() = true

}
