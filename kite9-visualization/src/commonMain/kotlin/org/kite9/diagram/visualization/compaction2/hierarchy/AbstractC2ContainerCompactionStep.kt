package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutingInfo
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

/**
 * This makes sure that any time we have all the groups to complete a container, we wrap the groups in the
 * container(s) and use that instead.
 */
abstract class AbstractC2ContainerCompactionStep(cd: CompleteDisplayer, val rr: RoutableReader) : AbstractC2BuilderCompactionStep(cd) {

    fun getEdgePosition(k: DiagramElement?, d: Direction) : Double? {
        return if (k != null) {
            val p = rr.getPlacedPosition(k) as PositionRoutingInfo?
            if (p != null) {
                when (d) {
                    Direction.UP -> p.getMinY()
                    Direction.DOWN -> p.getMaxY()
                    Direction.LEFT -> p.getMinX()
                    Direction.RIGHT -> p.getMaxX()
                }
            } else {
                null
            }
        } else {
            null
        }
    }

    fun findClosestToEdge(c: Container, d: Direction) : Set<DiagramElement> {
        val contents = c.getContents().filterIsInstance<Connected>()

        val positions = contents
            .map { e -> e to getEdgePosition(e, d) }
            .filter { (_, v) -> v != null }
            .map { (k, v) -> k to v!! }

        val positionNumbers = positions.map { (k, v) -> v }

        if (positionNumbers.isNotEmpty()) {
            val maxPosition = positionNumbers.maxOf { it }
            val minPosition = positionNumbers.minOf { it }

            val relevantPositions = positions.filter {
                when (d) {
                    Direction.UP, Direction.LEFT -> it.second == minPosition
                    Direction.RIGHT, Direction.DOWN -> it.second == maxPosition
                }
            }

            return relevantPositions.map { it.first }.toSet()
        } else {
            return emptySet()
        }
    }

    fun applyContainerEdge(so: C2SlackOptimisation, c: Container, to: Set<DiagramElement>, s: Side, map: MutableMap<LeafGroup, Pair<RoutableSlideableSet?, RoutableSlideableSet?>>, dimension: Dimension, topGroup: Group, elementMapping: MutableMap<DiagramElement, Set<LeafGroup>>) {
        val groups = to.flatMap { elementMapping[it] ?: emptySet() }

        groups.forEach { lg ->
            val routables = map[lg]!!
            val inside = checkCreateElement(c, dimension, so, null, topGroup)
            val theRSS = if (dimension == Dimension.H) routables.first else routables.second
            if (theRSS != null) {
                val newRSS = so.addSide(inside, theRSS, s)
                if (s == Side.START) {
                    if (theRSS.bl != null) {
                        so.ensureMinimumDistance(inside.l, theRSS.bl!!, 0)
                    }
                } else {
                    if (theRSS.br != null) {
                        so.ensureMinimumDistance(theRSS.br!!, inside.r, 0)
                    }
                }
                if (dimension == Dimension.H) {
                    map[lg] = Pair(newRSS, routables.second)
                } else {
                    map[lg] = Pair(routables.first, newRSS)
                }
            }
        }
    }

    private fun completeContainers(
        c: Container,
        co: C2Compaction,
        map: MutableMap<LeafGroup, Pair<RoutableSlideableSet?, RoutableSlideableSet?>>,
        topGroup: Group,
        elementMapping: MutableMap<DiagramElement, Set<LeafGroup>>) {
        println("Completing ${c}")
        val pp = rr.getPlacedPosition(c)
        if (pp == null) {
            c.getContents()
                .filterIsInstance<Container>()
                .forEach { completeContainers(it, co, map, topGroup, elementMapping) }

            val allLeafGroups = c.getContents()
                .flatMap { elementMapping[it] ?: emptySet() }
                .toSet()

            elementMapping[c] = allLeafGroups

            val leftMost = findClosestToEdge(c, Direction.LEFT)
            val rightMost = findClosestToEdge(c, Direction.RIGHT)
            val topMost = findClosestToEdge(c, Direction.UP)
            val bottomMost = findClosestToEdge(c, Direction.DOWN)

            if ((leftMost.isNotEmpty()) && (topMost.isNotEmpty())) {
                val sox = co.getSlackOptimisation(Dimension.H)
                val soy = co.getSlackOptimisation(Dimension.V)

                applyContainerEdge(sox, c, leftMost, Side.START, map, Dimension.H, topGroup, elementMapping)
                applyContainerEdge(sox, c, rightMost, Side.END, map, Dimension.H, topGroup, elementMapping)
                applyContainerEdge(soy, c, topMost, Side.START, map, Dimension.V, topGroup, elementMapping)
                applyContainerEdge(soy, c, bottomMost, Side.END, map, Dimension.V, topGroup, elementMapping)

                val containerBounds = c.getContents()
                    .map { rr.getPlacedPosition(it) }
                    .reduceOrNull { a, b ->
                        if ((a != null) && (b != null)) {
                            rr.increaseBounds(a, b)
                        } else {
                            a ?: b
                        }
                    }

                (rr as RoutableHandler2D).setPlacedPosition(c, containerBounds!!)
            }
        }
    }

    private fun ensurePositionsOfElements(mapping: Map<DiagramElement, Set<LeafGroup>>) {
        mapping.forEach { (k, v) ->
            val pos = rr.getPlacedPosition(v.first())
            (rr as RoutableHandler2D).setPlacedPosition(k, pos!!)
        }
    }

    fun wrapContainersIntoGroups(c2: C2Compaction, map: Map<LeafGroup, Pair<RoutableSlideableSet?, RoutableSlideableSet?>>, topGroup: Group) : Map<LeafGroup, Pair<RoutableSlideableSet?, RoutableSlideableSet?>> {
        val elementMapping = relevantElements(map.keys)
        ensurePositionsOfElements(elementMapping)
        val mutableMap = map.toMutableMap()
        completeContainers(c2.getDiagram(), c2, mutableMap, topGroup, elementMapping)
        return mutableMap
    }

    private fun relevantElements(all: Set<LeafGroup>) : MutableMap<DiagramElement, Set<LeafGroup>> {
        return all
            .filter { it.connected is Rectangular}
            .map { it.connected as DiagramElement to setOf(it) }
            .toMap()
            .toMutableMap()
    }

}