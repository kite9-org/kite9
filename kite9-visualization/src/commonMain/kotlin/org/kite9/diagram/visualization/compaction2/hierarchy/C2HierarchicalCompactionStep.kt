package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

class C2HierarchicalCompactionStep(cd: CompleteDisplayer,  rr: RoutableReader, gp: GridPositioner) : AbstractC2ContainerCompactionStep(cd, rr, gp) {

    var first = true
    var leafGroupElements : Set<DiagramElement> = emptySet()

    override fun compact(c: C2Compaction, g: Group) {
        if (!first) {
            return
        }

        first = true

        fun distinctOperation(g: Group) : Pair<DiagramElement?, Int?> {
            return when (g) {
                is CompoundGroup -> Pair(null, g.groupNumber)
                is LeafGroup if g.connected != null -> Pair(g.connected, 1)
                else -> Pair((g as LeafGroup).container, 2)
            }
        }

        val allGroups = collectGroups(g)
            .distinctBy { distinctOperation(it) }

        val leafGroups = allGroups
            .filterIsInstance<LeafGroup>()

        leafGroupElements = leafGroups.map { it.connected }.toSet()

        val leafGroupMap = leafGroups.map { it to processLeafGroup(it, c, g) }.toMap()
        val wrappedLeafGroupMap = wrapContainersIntoGroups(c, leafGroupMap, g)

        val left = alignLeafGroups(wrappedLeafGroupMap, Direction.LEFT)
        val right = alignLeafGroups(wrappedLeafGroupMap, Direction.RIGHT)
        val up = alignLeafGroups(wrappedLeafGroupMap, Direction.UP)
        val down = alignLeafGroups(wrappedLeafGroupMap, Direction.DOWN)

        val left2 = mergeSide(left, Side.START, c.getSlackOptimisation(Dimension.H))
        val right2 = mergeSide(right, Side.END, c.getSlackOptimisation(Dimension.H))
        val up2 = mergeSide(up, Side.START, c.getSlackOptimisation(Dimension.V))
        val down2 = mergeSide(down, Side.END, c.getSlackOptimisation(Dimension.V))

        joinSides(right2, left2, c.getSlackOptimisation(Dimension.H), Dimension.H)
        joinSides(down2, up2, c.getSlackOptimisation(Dimension.V), Dimension.V)

        // stitch together orbits from next-to containers in a grid
        handleGridNeighbours(c)
    }

    private fun handleGridNeighbours(c: C2Compaction) {
        gridOrbitSlideables.keys.forEach {
            if (it.s == Side.START) {
                val counterpart = Quad(it.c, it.i - 1, it.d, Side.END)
                val slideableA = C2Slideable.getNonDoneVersion(gridOrbitSlideables[counterpart])
                val slideableB = C2Slideable.getNonDoneVersion(gridOrbitSlideables[it])
                c.copyNeighbourMap(slideableB, slideableA)
                c.copyNeighbourMap(slideableA, slideableB)
            } else {
                val counterpart = Quad(it.c, it.i + 1, it.d, Side.START)
                val slideableA = C2Slideable.getNonDoneVersion(gridOrbitSlideables[counterpart])
                val slideableB = C2Slideable.getNonDoneVersion(gridOrbitSlideables[it])
                c.copyNeighbourMap(slideableB, slideableA)
                c.copyNeighbourMap(slideableA, slideableB)
            }
        }
    }

    fun mergeSide(leaves: Map<Double, Set<RoutableSlideableSet>>, s: Side, so: C2SlackOptimisation) : Map<Double, C2Slideable?> {

        fun ensureOrbitSlideableSet(s: RoutableSlideableSet, side: Side) : RoutableSlideableSet {
            val slideable = if (side == Side.START) s.bl else s.br

            if (slideable == null) {
                return s;
            }

            val slideableToCheck = slideable.getNotDoneVersion()
            if (slideableToCheck.getRectAnchors().isNotEmpty()) {
                // ok, get the previous version and test again
                val previous = s.previous
                if (previous == null) {
                    throw LogicException("Should be a wrapper slideable set")
                } else {
                    return ensureOrbitSlideableSet(previous, side)
                }
            } else {
                return s
            }
        }

        fun mergeSlideablesInMap(l2: Map<Double, Set<RoutableSlideableSet>>) : Map<Double, C2Slideable?> {
            val out = l2
                    .mapValues { (_, r) -> r.map {
                        if (s == Side.START) {
                            C2Slideable.getNonDoneVersion(it.bl)
                        } else {
                            C2Slideable.getNonDoneVersion(it.br)
                        }
                    }.reduceOrNull {
                            a, b -> so.mergeSlideables(a, b)
                    }
                }
            return out
        }

        val orbitLeaves = leaves.mapValues { (_, r) -> r.map { ensureOrbitSlideableSet(it, s) }.toSet() }
        val rectLeaves = leaves.mapValues { (k, v) -> v.minus(orbitLeaves[k] ?: emptySet()) }

        val mergedOrbitLeaves = mergeSlideablesInMap(orbitLeaves)
        var mergedRectLeaves = mergeSlideablesInMap(rectLeaves)

        // which to return?  Choose rect if it's available as it will be the grid around the orbit.
        val combinedLeaves = mergedOrbitLeaves.mapValues { (k,v) ->
            val rv = mergedRectLeaves[k]
            rv ?: v
        }

        return combinedLeaves
    }

    fun joinSides(start: Map<Double, C2Slideable?>, end: Map<Double, C2Slideable?>, so: C2SlackOptimisation, d: Dimension) {
        val allKeys = start.keys + end.keys
        allKeys.forEach { k ->
            val startS = start[k]
            val endS = end[k]

            if ((startS != null) && (endS != null)) {
                val startR = startS.getOrbitingElements()
                val endR = endS.getOrbitingElements()

                so.mergeSlideables(startS, endS)

                // ensure Separation of rectangulars
                val distance = if (startR.isNotEmpty() && endR.isNotEmpty()) {
                    startR.maxOf { ae ->
                        endR.maxOf {
                            be -> getMinimumDistanceBetween(ae, Side.END, be, Side.START, d, null, true)
                        }
                    }
                } else {
                    2.0
                }

                val startRects = startR.map {
                    val rs = so.getSlideablesFor(it as Positioned)
                    rs?.r
                }.filterNotNull()

                val endRects = endR.map {
                    val ls = so.getSlideablesFor(it as Positioned)
                    ls?.l
                }.filterNotNull()

                startRects.forEach { aS ->
                    endRects.forEach { bS ->
                        so.ensureMinimumDistance(aS, bS, distance.toInt())
                    }
                }
            }
        }

    }

    fun alignLeafGroups(
        contents: Map<LeafGroup, Pair<RoutableSlideableSet?, RoutableSlideableSet?>>,
        d: Direction
    ): Map<Double, Set<RoutableSlideableSet>> {

        val grouped: Map<Double?, List<RoutableSlideableSet?>> =
            contents.entries.groupBy(
                keySelector = { (k, _) -> getEdgePosition(k, d) },
                valueTransform = { (_, v) ->
                    when (d) {
                        Direction.UP, Direction.DOWN -> v.second
                        Direction.LEFT, Direction.RIGHT -> v.first
                    }
                }
            )

        return grouped
            .filterKeys { it != null }
            .mapKeys { (k, _) -> k!! }
            .mapValues { (_, list) ->
            list.filterNotNull().toSet()
        }
    }

    fun processLeafGroup(g: LeafGroup, c: C2Compaction, topGroup: Group) : Pair<RoutableSlideableSet?, RoutableSlideableSet?> {
        val e = g.connected
        if (e is Rectangular) {
            val hso = c.getSlackOptimisation(Dimension.H)
            val vso = c.getSlackOptimisation(Dimension.V)
            val hr = checkCreateElement(e, Dimension.H, hso, null, topGroup)
            val vr = checkCreateElement(e, Dimension.V, vso, null, topGroup)

            val hss = hr.wrapInRoutable()
            val vss = vr.wrapInRoutable()

            hso.add(hss)
            vso.add(vss)

            createRoutableNeighbours(c, hss, vss, hr, vr)

            if ((hss != null) && (vss != null)) {
                hso.contains(hss, hr)
                vso.contains(vss, vr)
            }

            return Pair(hss, vss)
        } else if (e is PlacementPositioned) {
            // leaf node is a port
            val f = g.container!!
            var pvi : RoutableSlideableSet? = null
            var phi : RoutableSlideableSet?  = null

            val vso = c.getSlackOptimisation(Dimension.V)
            pvi = checkCreateIntersectionOnly(vso, g, f, Dimension.V)
            val vr = checkCreateElement(f, Dimension.V, vso, null, topGroup)
            ensureCentreSlideablePosition(vso, vr, pvi.c, e.getContainerPosition(Dimension.V))

            val hso = c.getSlackOptimisation(Dimension.H)
            phi = checkCreateIntersectionOnly(hso, g, f, Dimension.H)
            val hr = checkCreateElement(f, Dimension.H, hso, null, topGroup)
            ensureCentreSlideablePosition(hso, hr, phi.c, e.getContainerPosition(Dimension.H))

            createRoutableNeighbours(c, phi, pvi, hr, vr)

            return Pair(phi, pvi)
        } else {
            throw LogicException("Can't process this element type")
        }
    }

    private fun collectGroups(g: Group) : List<Group> {
        return when (g) {
            is CompoundGroup -> listOf(g) + collectGroups(g.a) + collectGroups(g.b)
            is LeafGroup -> listOf(g)
        }
    }

    /**
     * Either content are laid out using the Group process, or they aren't connected, so we need
     * to just follow layout.
     */
    override fun usingGroups(contents: List<ConnectedRectangular>, topGroup: Group?) : Boolean {
        val gm = contents.map { hasGroup(it) }
        return gm.reduceRightOrNull {  a, b -> a && b } ?: false
    }

    private fun hasGroup(item: DiagramElement) : Boolean {
        if (leafGroupElements.contains(item)) {
            return true
        }

        if (item is Container) {
            val contents = item.getContents()
            if (contents.firstOrNull() { hasGroup(it) } != null) {
                return true
            }
        }

        return false
    }

    override val prefix: String
        get() = "HIER"

    override val isLoggingEnabled: Boolean
        get() = true
}