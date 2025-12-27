package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.anchors.Purpose
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSetImpl
import org.kite9.diagram.visualization.compaction2.sets.SlideableSet
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

class C2HierarchicalCompactionStep(cd: CompleteDisplayer, r: GroupResult, rr: RoutableReader) : AbstractC2ContainerCompactionStep(cd, r, rr) {

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

        leafGroups.forEach { processLeafGroup(it, c, g) }
        wrapContainersIntoGroups(c, g)

        val horiz = bucketLeafGroups(leafGroups, true)
        val vert = bucketLeafGroups(leafGroups, false)

        val horiz2 = horiz.map {
            it.map { g ->
                val so = c.getSlackOptimisation(Dimension.H)
                val ga = so.getSlideablesFor(g).lastOrNull()
                so.remove(g)
                ga
            }.filterNotNull().reduceOrNull { a, b ->
                mergeForAxis(c, a, b, Dimension.H, true)
            }
        }

        val vert2 = vert.map {
            it.map { g ->
                val so = c.getSlackOptimisation(Dimension.V)
                val ga = so.getSlideablesFor(g).lastOrNull()
                so.remove(g)
                ga
            }.filterNotNull().reduceOrNull {
                a, b -> mergeForAxis(c, a, b, Dimension.V, true)
            }
        }

        horiz2.filterNotNull().reduce { a, b -> mergeForAxis(c, a, b, Dimension.H, false) }
        vert2.filterNotNull().reduce { a, b -> mergeForAxis(c, a, b, Dimension.V, false) }
    }

    fun bucketLeafGroups(contents: List<LeafGroup>, horiz: Boolean) : List<Set<LeafGroup>> {
        val buckets = mutableListOf<MutableSet<LeafGroup>>()
        contents.forEach { e ->
            val b = buckets.firstOrNull {
                val bp = this.rr.getPlacedPosition(it.first())
                val ep = this.rr.getPlacedPosition(e)
                if ((bp != null) && (ep != null)) {
                    val res = this.rr.isInPlane(bp, ep, horiz)
                    res
                } else {
                    throw LogicException("oops null")
                }
            }

            if (b != null) {
                b.add(e)
            } else {
                val newB = mutableSetOf<LeafGroup>(e)
                buckets.add(newB)
            }
        }

        return buckets.sortedBy {
            val bp = this.rr.getPlacedPosition(it.first())!!
            if (horiz)
                bp.centerX()
            else
                bp.centerY()
        }
    }

    fun processLeafGroup(g: LeafGroup, c: C2Compaction, topGroup: Group) {
        val e = g.connected
        if (e is Rectangular) {
            val hso = c.getSlackOptimisation(Dimension.H)
            val vso = c.getSlackOptimisation(Dimension.V)
            val hr = checkCreateElement(e, Dimension.H, hso, null, g)
            val vr = checkCreateElement(e, Dimension.V, vso, null, g)

            val hss = hr.wrapInRoutable()
            val vss = vr.wrapInRoutable()
            if ((hss != null) && (vss != null)) {
                hso.add(g, hss)
                vso.add(g, vss)
                hso.contains(hss, hr)
                vso.contains(vss, vr)
            }
        } else if (e is Port) {
            // leaf node is a port
            val f = g.container!!
            val direction = e.getPortDirection()
            when (direction) {
                Direction.LEFT, Direction.RIGHT -> {
                    val vso = c.getSlackOptimisation(Dimension.V)
                    val pvi = checkCreateIntersectionOnly(vso, g, f, Dimension.V)
                    val vr = checkCreateElement(f, Dimension.V, vso, null, topGroup)
                    ensurePortSlideablePosition(vso, vr, pvi.c)
                }
                Direction.UP, Direction.DOWN -> {
                    val hso = c.getSlackOptimisation(Dimension.H)
                    val phi = checkCreateIntersectionOnly(hso, g, f, Dimension.H)
                    val hr = checkCreateElement(f, Dimension.H, hso, null, topGroup)
                    ensurePortSlideablePosition(hso, hr, phi.c)
                }
            }
        }
        else {
            // leaf node must be for container arrival
            val f = g.container!!
            checkCreateIntersectionOnly(c.getSlackOptimisation(Dimension.H), g, f, Dimension.H)
            checkCreateIntersectionOnly(c.getSlackOptimisation(Dimension.V), g, f, Dimension.V)
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

    private fun checkCreateIntersectionOnly(cso: C2SlackOptimisation, g: LeafGroup, c: Container, d: Dimension) : RoutableSlideableSet {
        val ss1 = cso.getSlideablesFor(g).lastOrNull()

        if (ss1 != null) {
            return ss1
        }

        val out = if (g.connected is Port) {
            val ic = C2Slideable(cso, d,  g.connected as Port, Purpose.PORT)
            val out2 = RoutableSlideableSetImpl(ic, null, null)
            cso.add(g.connected as Port, out2)
            out2
        } else {
            val ic = C2Slideable(cso, d,  c as Connected, Purpose.PORT)
            RoutableSlideableSetImpl(ic, null, null)
        }

        cso.add(g, out)


        log.send("Created a RoutableSlideableSet for $c: ", out.getAll())
        return out
    }


    private fun mergeForAxis(c: C2Compaction, ha: RoutableSlideableSet, hb : RoutableSlideableSet, d: Dimension, overlap: Boolean) : RoutableSlideableSet {
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