package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.SizedRectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.compaction2.anchors.Permeability
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader
import org.kite9.diagram.visualization.planarization.rhd.grouping.TemporaryContainerHub
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutingInfo
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

/**
 * This makes sure that any time we have all the groups to complete a container, we wrap the groups in the
 * container(s) and use that instead.
 */
abstract class AbstractC2ContainerCompactionStep(cd: CompleteDisplayer, val rr: RoutableReader, gp: GridPositioner) : AbstractC2BuilderCompactionStep(cd, gp) {

    fun getEdgePosition(k: LeafGroup, d: Direction) : Double? {
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

    fun findClosestToEdge(c: Container, d: Direction, allLeafGroups: Set<LeafGroup>) : Set<LeafGroup> {
        val positions = allLeafGroups
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

    private fun getPadding(c: Container, s: Side, d: Dimension) : Int {
        val dir = if (s == Side.START) {
            if (d == Dimension.H) {
                Direction.LEFT
            } else {
                Direction.UP
            }
        } else {
            if (d == Dimension.H) {
                Direction.RIGHT
            } else {
                Direction.DOWN
            }
        }

        return if (c is SizedRectangular) {
            (c.getPadding(dir) / 2.0).toInt()
        } else {
            1
        }
    }

    fun applyContainerEdge(so: C2SlackOptimisation,
                           c: Container,
                           to: Set<LeafGroup>,
                           s: Side,
                           map: MutableMap<LeafGroup, Pair<RoutableSlideableSet?, RoutableSlideableSet?>>,
                           dimension: Dimension,
                           topGroup: Group) {

        val allMergableRoutables = mutableSetOf<C2Slideable>()
        var hub : TemporaryContainerHub? = null


        to.forEach { lg ->
            if (lg.connected is TemporaryContainerHub) {
                hub = lg.connected as TemporaryContainerHub
            }
            val routables = map[lg]!!
            val outer = checkCreateElement(c, dimension, so, null, topGroup)
            val theRSS = if (dimension == Dimension.H) routables.first else routables.second
            val otherRSS = if (dimension == Dimension.V) routables.first else routables.second
            if (theRSS != null) {
                val isGridCell = (c.getParent() as Container?)?.getLayout() == Layout.GRID
                val useOrbit = !isGridCell
                val padding = getPadding(c, s, dimension)
                val newRSS = so.addSide(outer, theRSS, s, useOrbit, padding)
                if (s == Side.START) {
                    val bl = theRSS.bl?.getNotDoneVersion()
                    if ((bl != null) && (bl != outer.l)) {
                        so.ensureMinimumDistance(outer.l.getNotDoneVersion(), bl!!, padding)
                    }
                    if (bl != null) {
                        allMergableRoutables.add(bl)
                    }
                } else {
                    val br = theRSS.br?.getNotDoneVersion()
                    if ((br != null) && (br != outer.r)) {
                        so.ensureMinimumDistance(br!!, outer.r, padding)
                    }
                    if (br != null) {
                        allMergableRoutables.add(br!!)
                    }
                }

                /*if (otherRSS?.c != null) {
                    if (s == Side.START) {
                        so.compaction.addNeighbour(otherRSS!!.c!!, outer.l, newRSS!!.bl)
                    } else {
                        so.compaction.addNeighbour(otherRSS!!.c!!, outer.r, newRSS!!.br)
                    }
                }*/

                if (dimension == Dimension.H) {
                    map[lg] = Pair(newRSS, routables.second)
                } else {
                    map[lg] = Pair(routables.first, newRSS)
                }
            }
        }

        val done = allMergableRoutables.reduceOrNull { a, b ->
            so.mergeSlideables(a, b)!!
        }

        if (c.getContainer()?.getLayout() == Layout.GRID) {
            if (hub == null) {
                throw LogicException("grid cell without hub")
            }

            val i = if (dimension == Dimension.H) hub?.gridPosition?.first else hub?.gridPosition?.second
            val q = Quad(c.getContainer()!!, i!!, dimension, s)
            val existing = gridOrbitSlideables[q]

            if (existing != null) {
                gridOrbitSlideables[q] = so.mergeSlideables(existing, done)!!
                ensureNoOldGridOrbitSlideables()
            } else {
                // we need to create a slideable
                throw LogicException("was expecting orbit slideable for grid element")
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

            val leftMost = findClosestToEdge(c, Direction.LEFT, allLeafGroups)
            val rightMost = findClosestToEdge(c, Direction.RIGHT, allLeafGroups)
            val topMost = findClosestToEdge(c, Direction.UP, allLeafGroups)
            val bottomMost = findClosestToEdge(c, Direction.DOWN, allLeafGroups)

            if ((leftMost.isNotEmpty()) && (topMost.isNotEmpty())) {
                val sox = co.getSlackOptimisation(Dimension.H)
                val soy = co.getSlackOptimisation(Dimension.V)

                applyContainerEdge(sox, c, leftMost, Side.START, map, Dimension.H, topGroup)
                applyContainerEdge(sox, c, rightMost, Side.END, map, Dimension.H, topGroup)
                applyContainerEdge(soy, c, topMost, Side.START, map, Dimension.V, topGroup)
                applyContainerEdge(soy, c, bottomMost, Side.END, map, Dimension.V, topGroup)

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

                val cx = sox.getSlideablesFor(c)
                val cy = soy.getSlideablesFor(c)
                if ((cx != null) && (cy != null)) {

                    val sx = sox.getContainers(cx)
                    val sy = soy.getContainers(cy)

                    if (sx.isNotEmpty() && sy.isNotEmpty()) {
                        val x1 = sx.first()
                        val y1 = sy.first()
                        createRoutableNeighbours(co, x1, y1, cx, cy)
                        co.invertNeighbours(x1.bl)
                        co.invertNeighbours(x1.br)
                        co.invertNeighbours(y1.bl)
                        co.invertNeighbours(y1.br)
                    }

                }
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
            //.filter { (it.connected is Rectangular) || (it.connected is TemporaryContainerHub)}
            .map { it.connected as DiagramElement to setOf(it) }
            .toMap()
            .toMutableMap()
    }

}