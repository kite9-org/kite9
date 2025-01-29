package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.Dimension.H
import org.kite9.diagram.common.elements.Dimension.V
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.compaction2.anchors.ConnAnchor
import org.kite9.diagram.visualization.compaction2.hierarchy.AbstractC2BuilderCompactionStep
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.links.RankBasedConnectionQueue
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D
import kotlin.math.max

class C2ConnectionFanningCompactionStep(cd: CompleteDisplayer, gp: GridPositioner) :
    AbstractC2CompactionStep(cd) {

    data class ConnectionSection(val from: ConnAnchor, val to: ConnAnchor) {


    }

    enum class Placement {
        OUTSIDE, INSIDE, ON
    }

    override fun compact(c: C2Compaction, g: Group) {
        val soV = c.getSlackOptimisation(V)
        val soH = c.getSlackOptimisation(H)

        soV.updateTDMatrix()
        soH.updateTDMatrix()

        val connAnchorMapV= prepareMap(soV)
        val connAnchorMapH = prepareMap(soH)

        val initialSlideablesV = soV.getAllSlideables().toList()
        initialSlideablesV.forEach { process(it, soV, V, soH, connAnchorMapH, c) }

        val initialSlideablesH = soH.getAllSlideables().toList()
        initialSlideablesH.forEach { process(it, soH, H,  soV, connAnchorMapV, c) }
    }
    private fun prepareMap(so1: C2SlackOptimisation): Map<ConnAnchor, C2Slideable> {
        return so1.getAllSlideables()
            .flatMap { s -> s.getConnAnchors().map { it to s } }
            .groupBy { it.first }
            .mapValues { it.value.map { it -> it.second }.first() }
    }


    private fun process(s: C2Slideable,  so: C2SlackOptimisation, d: Dimension,  soPerp: C2SlackOptimisation, connAnchorMap: Map<ConnAnchor, C2Slideable>, c: C2Compaction) {
        val connections = s.getConnAnchors().map { it -> it.e }.toSet()
        val sections = mutableListOf<ConnectionSection>()
        val diagramConnectionOrdering = orderSlideables(connections, so, c)
        val transitiveDistances = soPerp.getTransitiveDistanceMatrix()

        diagramConnectionOrdering.forEach { c ->
            val anchorsForConnection = s.getConnAnchors().filter { it.e == c }.sortedBy { it.s }
            val indexes = anchorsForConnection.map { it.s }
            indexes.forEach { i ->
                if (indexes.contains(i - 1)) {
                    // we have a section
                    val from = anchorsForConnection[indexes.indexOf(i - 1)]
                    val to = anchorsForConnection[indexes.indexOf(i)]
                    sections.add(ConnectionSection(from, to))
                }
            }
        }

        fun overlaps(toAdd: ConnectionSection, currentLane: List<ConnectionSection>): Boolean {

            fun dist(p1: C2Slideable, p2: C2Slideable): Constraint? {
                return transitiveDistances[p1]?.get(p2)
            }

            fun dist(c1: ConnAnchor, c2: ConnAnchor): Int? {
                val s1 = connAnchorMap[c1]!!
                val s2 = connAnchorMap[c2]!!
                if (s1==s2) {
                    return 0
                }
                val c = dist(s1, s2)
                if (c == null) {
                    return null
                } else if (c.forward) {
                    return c.dist
                } else {
                    return -c.dist
                }
            }



            fun between(mid: ConnAnchor, a: ConnAnchor, b: ConnAnchor): Placement {
                val midToA = dist(mid, a)
                val midToB = dist(mid, b)

                if ((midToA == null) || (midToB == null)) {
                    return Placement.OUTSIDE
                }

                if ((midToA == 0) || (midToB == 0)) {
                    return Placement.ON
                }

                val midToAForward = midToA >0
                val midToBForward = midToB >0

                val midToABackward = midToB <0
                val midToBBackward = midToB <0

                if (midToAForward && midToBBackward) {
                    return Placement.INSIDE
                } else if (midToABackward && midToBForward) {
                    return Placement.INSIDE
                } else {
                    return Placement.OUTSIDE
                }
            }

            fun inside(inner: ConnectionSection, outer: ConnectionSection) : Boolean {
                val first = between(inner.from, outer.from, outer.to)
                val second = between(inner.to, outer.from, outer.to)
                return if ((first == Placement.INSIDE) || (second == Placement.INSIDE)) {
                    true
                } else (first == Placement.ON) && (second == Placement.ON)
            }

            val overlaps =
                currentLane.filter {
                    return inside(it, toAdd) || inside(toAdd, it)

                }
            return overlaps.isNotEmpty()
        }

        if ((sections.size > 1) && (connections.size > 1)) {
            // ok, we have overlaps on this section.
            val lanes = mutableListOf<MutableList<ConnectionSection>>()

            sections.forEach { si ->
                // assign the sections to different lanes
                if (lanes.isEmpty() || overlaps(si, lanes.last())) {
                    lanes.add(mutableListOf(si))
                } else {
                    lanes.last().add(si)
                }
            }

            // now map lanes to new slideables
            val middleLane = (lanes.size / 2)
            val slideables = lanes.mapIndexed { i, connectionSections ->
                val connAnchors = connectionSections.flatMap { listOf(it.to, it.from) }.toSet()
                if (i == middleLane) {
                    s.replaceConnAnchors(connAnchors)
                    s
                } else {
                    val o = C2Slideable(so, d, connAnchors )
                    so.addSlideable(o)
                    o
                }
            }

            // ensure place within diagram
            if (s != slideables.first()) {
                so.copyMinimumConstraints(s, slideables.first())
            }
            if (s != slideables.last()) {
                so.copyMinimumConstraints(s, slideables.last())
            }

            // ensure distance between each one
            slideables.forEachIndexed { i, s2 ->
                if (i > 0) {
                    val s1 = slideables[i - 1]
                    so.ensureMinimumDistance(s1, s2, 3)
                }
            }

        }
    }



    private fun orderSlideables(connections: Set<Connection>, so: C2SlackOptimisation, c: C2Compaction): List<Connection> {
        return connections.toList()
    }

    override val prefix = "C2CF"
    override val isLoggingEnabled = true


}