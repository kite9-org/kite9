package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSetImpl
import org.kite9.diagram.visualization.compaction2.sets.SlideableSet
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

class C2HierarchicalCompactionStep(cd: CompleteDisplayer, r: GroupResult) : AbstractC2ContainerCompactionStep(cd, r) {

    var first = true
    override fun compact(c: C2Compaction, g: Group) {
        if (!first) {
            return
        }

        first = true

        // first, collect all groups and order
        val allGroups = collectGroups(g).sortedBy { it.height }.distinctBy { it.groupNumber }
        allGroups.forEach { processGroup(it, c) }
    }

    private fun processGroup(
        g: Group,
        c: C2Compaction
    ) {
        c.checkConsistency()
        if (g is CompoundGroup) {
            if (horizontalAxis(g)) {
                mergeForAxis(c, g, Dimension.H, Layout.RIGHT, Layout.LEFT)
            }

            if (verticalAxis(g)) {
                mergeForAxis(c, g, Dimension.V, Layout.DOWN, Layout.UP)
            }

            if (!horizontalAxis(g) && !verticalAxis(g)) {
                // in this case, we need to store whatever is available.
                val slackOptimisationV = c.getSlackOptimisation(Dimension.V)
                val va = slackOptimisationV.getSlideablesFor(g.a)
                val vb = slackOptimisationV.getSlideablesFor(g.b)

                val slackOptimisationH = c.getSlackOptimisation(Dimension.H)
                val ha = slackOptimisationH.getSlideablesFor(g.a)
                val hb = slackOptimisationH.getSlideablesFor(g.b)

                if ((ha != null) && (hb != null)) {
                    throw LogicException("Need to rethink this")
                }

                slackOptimisationV.add(g, va ?: vb!!)
                slackOptimisationH.add(g, ha ?: hb!!)
                slackOptimisationV.remove(g.a)
                slackOptimisationV.remove(g.b)
                slackOptimisationH.remove(g.a)
                slackOptimisationH.remove(g.b)
            }
        } else if (g is LeafGroup) {
            // leaf group
            val e = g.connected
            if (e is Rectangular) {
                val hso = c.getSlackOptimisation(Dimension.H)
                val vso = c.getSlackOptimisation(Dimension.V)
                val hr = checkCreateElement(e, Dimension.H, hso, null, g)!!
                val vr = checkCreateElement(e, Dimension.V, vso, null, g)!!

                val hss = hr.wrapInRoutable(c, g)
                val vss = vr.wrapInRoutable(c, g)
                if ((hss != null) && (vss != null)) {
                    hso.contains(hss, hr)
                    vso.contains(vss, vr)
                    c.setupLeafRectangularIntersections(hss, vss, hr, vr)
                    c.setupRoutableIntersections(hss, vss)
                }
            } else {
                // leaf node must be for container arrival
                val f = g.container!!
                val hss = checkCreateIntersectionOnly(c.getSlackOptimisation(Dimension.H), g, f, Dimension.H)
                val vss = checkCreateIntersectionOnly(c.getSlackOptimisation(Dimension.V), g, f, Dimension.V)
                c.setupRoutableIntersections(hss, vss)
            }

        } else {
            throw LogicException("Unknown group type")
        }

        if (horizontalAxis(g) || isTopmostGroup(g)) {
            completeContainers(c, g, Dimension.H)
        }

        if (verticalAxis(g) || isTopmostGroup(g)) {
            completeContainers(c, g, Dimension.V)
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
        val ss1 = cso.getSlideablesFor(g)

        if (ss1 != null) {
            return ss1
        }

        val icStart = C2Slideable(cso, d, g, c, setOf(Side.START))
        val icEnd = C2Slideable(cso, d, g, c, setOf(Side.END))

        val out = RoutableSlideableSetImpl(setOf(icStart, icEnd), null, null)

        cso.add(g, out)
        log.send("Created a RoutableSlideableSet for $c: ", out.getAll())
        return out
    }


    private fun mergeForAxis(c: C2Compaction, g: CompoundGroup, d: Dimension, l1: Layout, l2: Layout) {
        val a = g.a
        val b = g.b

        val so = c.getSlackOptimisation(d)
        val ha = so.getSlideablesFor(a)
        val hb = so.getSlideablesFor(b)

        val hm = if ((ha == null) || (hb == null)) {
            ha ?: hb
        } else {
            when (val rl = getRequiredLayout(g, d)) {
                l1 -> {
                    separateRectangular(ha, hb, so, d)
                    ha.mergeWithGutter(hb, so)
                }

                l2 -> {
                    separateRectangular(hb, ha, so, d)
                    hb.mergeWithGutter(ha, so)
                }

                null -> {
                    hb.mergeWithOverlap(ha, so)
                }

                else -> {
                    throw LogicException("Shouldn't get an $d merge of $rl for group $g")
                }
            }
        }

        so.remove(a)
        so.remove(b)

        if (hm != null) {
            so.add(g, hm)
        }
        so.checkConsistency()
    }

    private fun getRequiredLayout(g: CompoundGroup, d: Dimension) : Layout? {
        return if (!g.axis.isLayoutRequired) {
            return null
        } else if (d==Dimension.H) {
            when (g.layout) {
                Layout.RIGHT, Layout.LEFT -> g.layout
                else -> null
            }
        } else {
            when (g.layout) {
                Layout.UP, Layout.DOWN -> g.layout
                else -> null
            }
        }
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
            .flatMap { r -> r.getRectangulars() }
            .map { it.e }

        val bElements = bSlideables
            .flatMap { r -> r.getRectangulars() }
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