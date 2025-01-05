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

    override fun compact(c: C2Compaction, g: Group) {
        val so1 = c.getSlackOptimisation(V)
        val so2 = c.getSlackOptimisation(H)

        so1.updateTDMatrix()
        so2.updateTDMatrix()

        val connAnchorMap1 = prepareMap(so1)
        val connAnchorMap2 = prepareMap(so2)

        so1.getAllSlideables().forEach { process(it, so1, V, so2, connAnchorMap2, c) }
        so2.getAllSlideables().forEach { process(it, so2, H,  so1, connAnchorMap1, c) }
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

            fun dist(c1: ConnAnchor, c2: ConnAnchor): Constraint? {
                return dist(connAnchorMap[c1]!!, connAnchorMap[c2]!!)
            }

            fun between(mid: ConnAnchor, a: ConnAnchor, b: ConnAnchor): Boolean {
                val midToA = dist(mid, a)
                val midToB = dist(mid, b)

                if ((midToA != null) && (midToB != null)) {
                    return midToA.forward != midToB.forward
                } else {
                    return false
                }
            }

            val overlaps =
                currentLane.filter { between(toAdd.from, it.from, it.to) || between(toAdd.to, it.from, it.to) }
            return overlaps.isNotEmpty()
        }

        if ((sections.size > 1) && (connections.size > 1)) {
            // ok, we have overlaps on this section.
            val lanes = mutableListOf<MutableList<ConnectionSection>>()

            sections.forEach { s ->
                // assign the sections to different lanes
                if (lanes.isEmpty() || overlaps(s, lanes.last())) {
                    lanes.add(mutableListOf(s))
                } else {
                    lanes.last().add(s)
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
                    C2Slideable(so, d, connAnchors )
                }
            }

            // ensure distance between each one
            slideables.forEachIndexed { i, s2 ->
                if (i > 0) {
                    val s1 = slideables[i - 1]
                    s1.addMinimumForwardConstraint(s2, 3)
                }
            }


            // ensure place within diagram
            if (s != slideables.first()) {
                so.copyConstraints(s, slideables.first())
            }
            if (s != slideables.last()) {
                so.copyConstraints(s, slideables.last())
            }
        }
    }



    private fun orderSlideables(connections: Set<Connection>, so: C2SlackOptimisation, c: C2Compaction): List<Connection> {
        return connections.toList()
    }

    override val prefix = "C2CF"
    override val isLoggingEnabled = true


}