package org.kite9.diagram.visualization.planarization.rhd.position

import org.kite9.diagram.common.algorithms.so.PositionChangeNotifiable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.objects.BasicBounds
import org.kite9.diagram.common.objects.Bounds
import org.kite9.diagram.common.objects.IntBounds
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * This handles the position of groups (or anything) by using SingleDistance constraints.
 *
 */
class DistanceBasedRoutableHandler2D : AbstractPositionRoutableReader(), LayeredRoutableHandler2D, RoutableHandler2D {

    data class Layer(val groups: Set<Group>) : PositionChangeNotifiable {
        var position = IntBounds(0)

        override fun changedPosition(newPos: Int) {
            this.position = IntBounds(newPos)
        }
    }

    private val xPositions = mutableSetOf<Layer>()
    private val yPositions = mutableSetOf<Layer>()

    override fun getPlacedPosition(r: Group): PositionRoutingInfo {
        return BoundsBasedPositionRoutingInfo(getBoundsOf(r, Dimension.H), getBoundsOf(r, Dimension.V))
    }

    override fun getBoundsOf(
        r: Group,
        d: Dimension
    ): Bounds {
        return when (d) {
            Dimension.H -> {
                val key = xPositions.firstOrNull { it.groups.contains(r) }
                key?.position ?: BasicBounds.EMPTY_BOUNDS
            }
            Dimension.V -> {
                val key = yPositions.firstOrNull { it.groups.contains(r) }
                key?.position ?: BasicBounds.EMPTY_BOUNDS
            }
        }
    }

    override fun setPlacedPosition(
        r: CompoundGroup,
        d: Direction?
    ) {
        TODO("Not yet implemented")
    }

    override fun createLayer(
        groupsInLayer: Set<Group>,
        d: Dimension
    ) {
        val layerSet = when (d) {
            Dimension.H -> xPositions
            Dimension.V -> yPositions
        }

        val allImplicatedLayers = layerSet.filter { it.groups.intersect(groupsInLayer).isNotEmpty() }
        val superGroup = allImplicatedLayers.flatMap { it.groups }.toSet()
        allImplicatedLayers.forEach { layerSet.remove(it) }
        val layer = Layer(superGroup + groupsInLayer)
        layerSet.add(layer)
    }

    override fun orderLayers(
        g1: Set<Group>,
        g2: Set<Group>,
        d: Dimension
    ) {
        if (d == Dimension.H) {
            val g1Layers = xPositions.filter { it.groups.intersect(g1).isNotEmpty() }
            val g2Layers = xPositions.filter { it.groups.intersect(g2).isNotEmpty() }
            g1.forEach { l1 ->
                l1.
            }
        } else {
            val g1Layers = yPositions.filter { it.groups.intersect(g1).isNotEmpty() }
            val g2Layers = yPositions.filter { it.groups.intersect(g2).isNotEmpty() }
            g1.forEach {

            }
        }
    }
}