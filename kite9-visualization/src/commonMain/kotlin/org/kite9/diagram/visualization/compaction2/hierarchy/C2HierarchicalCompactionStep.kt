package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.compaction2.anchors.Purpose
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSetImpl
import org.kite9.diagram.visualization.compaction2.sets.SlideableSet
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader
import org.kite9.diagram.visualization.planarization.rhd.grouping.TemporaryContainerHub
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

class C2HierarchicalCompactionStep(cd: CompleteDisplayer,  rr: RoutableReader) : AbstractC2ContainerCompactionStep(cd, rr) {

    var first = true
    override fun compact(c: C2Compaction, g: Group) {
        if (!first) {
            return
        }

        first = true

        fun distinctOperation(g: Group) : Pair<DiagramElement?, Int?> {
            return when {
                g is CompoundGroup -> Pair(null, g.groupNumber)
                g is LeafGroup && g.connected != null -> Pair(g.connected, 1)
                else -> Pair((g as LeafGroup).container, 2)
            }
        }

        val allGroups = collectGroups(g)
            .distinctBy { distinctOperation(it) }

        val leafGroups = allGroups
            .filterIsInstance<LeafGroup>()

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
    }

    fun mergeSide(leaves: Map<Double, Set<RoutableSlideableSet>>, s: Side, so: C2SlackOptimisation) : Map<Double, C2Slideable?> {
        return leaves.mapValues { (k, r) ->
            r.map {
                if (s == Side.START) {
                    it.bl
                } else {
                    it.br
                }
            }
            .reduce {
                a, b -> so.mergeSlideables(a, b)
            }
        }
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
                    rs!!.r
                }

                val endRects = endR.map {
                    val ls = so.getSlideablesFor(it as Positioned)
                    ls!!.l
                }

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

            return Pair(phi, pvi)
        } else {
            throw LogicException("Can't process this element type")
        }
    }

    private fun isTopmostGroup(g: Group): Boolean {
        // if it's the top group, then all containers need to be completed
        return g.isActive()
    }

    private fun collectGroups(g: Group) : List<Group> {
        return when (g) {
            is CompoundGroup -> listOf(g) + collectGroups(g.a) + collectGroups(g.b)
            is LeafGroup -> listOf(g)
        }
    }



    private fun mergeForAxis(c: C2Compaction, ha: RoutableSlideableSet, hb : RoutableSlideableSet, d: Dimension, s: Side, overlap: Boolean) : RoutableSlideableSet {
        val so = c.getSlackOptimisation(d)

        val hm = if (!overlap) {
            separateRectangular(ha, hb, so, d)
            ha.mergeWithGutter(hb, so)
        } else {
            hb.mergeWithOverlap(ha, so)
        }

        so.checkConsistency()
        return hm!!
    }

    private fun separateRectangular(
        a: SlideableSet<*>,
        b: SlideableSet<*>,
        cso: C2SlackOptimisation,
        d: Dimension
    ) {
        val aSlideables =  cso.getRectangularsOnSide(Side.END,a)
        val bSlideables = cso.getRectangularsOnSide(Side.START, b)

        val aElements = aSlideables
            .flatMap { r -> r.getRectAnchors() }
            .map { it.e }

        val bElements = bSlideables
            .flatMap { r -> r.getRectAnchors() }
            .map { it.e }

        val distance = aElements.maxOfOrNull { ae ->
            bElements.maxOf { be -> getMinimumDistanceBetween(ae, Side.END, be, Side.START, d, null, true) }
        } ?: 2.0

        aSlideables.forEach { aS ->
            bSlideables.forEach { bS ->
                cso.ensureMinimumDistance(aS, bS, distance.toInt())
            }
        }
    }

    /**
     * Either content are laid out using the Group process, or they aren't connected, so we need
     * to just follow layout.
     */
    override fun usingGroups(contents: List<ConnectedRectangular>, topGroup: Group?) : Boolean {
        val gm = contents.map { hasGroup(it, topGroup) }
        return gm.reduceRightOrNull {  a, b -> a && b } ?: false
    }

    private fun hasGroup(item: Connected, group: Group?) : Boolean {
        return when (group) {
            is CompoundGroup -> {
                hasGroup(item, group.a) || hasGroup(item, group.b)
            }

            is LeafGroup -> {
                (group.container == item) || (group.connected == item)
            }

            else -> {
                false
            }
        }
    }

    override val prefix: String
        get() = "HIER"

    override val isLoggingEnabled: Boolean
        get() = true
}