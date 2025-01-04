package org.kite9.diagram.visualization.compaction2.routing

import org.kite9.diagram.common.elements.Dimension
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

    override fun compact(c: C2Compaction, g: Group) {
//        compact(c.getSlackOptimisation(Dimension.V), c)
//        compact(c.getSlackOptimisation(Dimension.H), c)
    }

    private fun compact(so: C2SlackOptimisation, c: C2Compaction) {
        val toProcess = so.getAllSlideables()
            .filter { it.getConnAnchors().map { a -> a.e }.toSet().size > 1 }

        toProcess.forEach { s ->
            val newSlideables = orderSlideables(s.getConnAnchors(), so, c)
                .map { C2Slideable(so, s.dimension, it) }

            // ensure distance between each one
            for (i in 1..newSlideables.size-1) {
                val s1 = newSlideables[i-1]
                val s2 = newSlideables[i]
                s1.addMinimumForwardConstraint(s2, 3)
            }

            // ensure place within diagram
            so.copyConstraints(s, newSlideables.first())
            so.copyConstraints(s, newSlideables.last())

            // handle start / end intersections
            val intersections = s.intersecting()

            s.getConnAnchors().filter { intersections.contains(it.e) }
                .forEach {
                    var connection = it.e
                }



        }
    }

    private fun orderSlideables(connAnchors: Set<ConnAnchor>, so: C2SlackOptimisation, c: C2Compaction): List<ConnAnchor> {
        return connAnchors.toList()
    }

    override val prefix = "C2CF"
    override val isLoggingEnabled = true


}