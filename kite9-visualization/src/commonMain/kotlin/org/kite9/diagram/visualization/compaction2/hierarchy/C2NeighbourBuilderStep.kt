package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import kotlin.math.max
import kotlin.math.min

class C2NeighbourBuilderStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override fun compact(c: C2Compaction, g: Group) {
        val d = c.getDiagram()
        //buildNeighbours(c, d, null, null)
        handleIntersections(c)
        c.joinOverlappingNeighbourGroups()
    }


//    private fun buildNeighbours(c: C2Compaction, d: DiagramElement, hss: RoutableSlideableSet?, vss: RoutableSlideableSet?) {
//        val hso = c.getSlackOptimisation(Dimension.H)
//        val vso = c.getSlackOptimisation(Dimension.V)
//
//        if (d is Positioned) {
//            val hr = hso.getSlideablesFor(d)
//            val vr = vso.getSlideablesFor(d)
//            if ((hr != null) && (vr != null)) {
//                if ((hss != null) && (vss != null)) {
//                    c.setupRectangularIntersections(hr, vr, hss, vss)
//                }
//
//                if (d is Container) {
//                    val hssi = hso.getContents(hr)
//                    val vssi = vso.getContents(vr)
//
//                    d.getContents().forEach { buildNeighbours(c, it, hssi, vssi) }
//
//                    if ((hssi != null) && (vssi != null)) {
//                        c.propagateIntersectionsBetweenRoutableAndOuterRectangular(hssi, vssi, hr, vr)
//                    }
//                }
//
//                val hcs = hso.getContainers(hr)
//                val vcs = vso.getContainers(vr)
//                val cs = hcs.flatMap { h -> vcs.map { v-> Pair(h,v) } }.toSet()
//
//                cs.forEach { (h, v) ->
//                    c.setupRoutableIntersections(h, v)
//                    c.propagateIntersectionsFromRectangularToOuterRoutable(hr, vr, h, v)
//                }
//            }
//        }
//    }

    private fun handleIntersections(c2: C2Compaction) {
        val v = c2.getSlackOptimisation(Dimension.V)
        val h = c2.getSlackOptimisation(Dimension.H)

        handleIntersectionsOnDimension(c2,v, h, Direction.RIGHT)
        handleIntersectionsOnDimension(c2, h, v, Direction.DOWN)
    }

    /**
     * The basic approach here is to trace along the intersection and make sure that every rectangular that crosses the path of it
     * intersects with the intersection.
     */
    private fun handleIntersectionsOnDimension(c2: C2Compaction,
                                               so: C2SlackOptimisation,
                                               sox: C2SlackOptimisation,
                                               d: Direction) {

        fun intersects(s: C2Slideable, from: C2Slideable, to: C2Slideable) : Boolean {
            return (s.minimumPosition >= min(from.minimumPosition, to.minimumPosition)) &&
                (s.minimumPosition <= max(from.minimumPosition, to.minimumPosition));
        }

        fun canEmergeFrom(s: C2Slideable, blockedBy: DiagramElement) : DiagramElement? {
            return if (s.getRectElements().contains(blockedBy)) {
                null
            } else {
                blockedBy
            }
        }

        fun canTraverse(s: C2Slideable, potentialBlockers: Set<C2Slideable>, alongElements: Set<DiagramElement>, d: Direction) : DiagramElement? {
            if (potentialBlockers.contains(s)) {
                val blocker = s.getRectAnchors()
                if (blocker.isEmpty()) {
                    return null
                } else if (blocker.size == 1) {
                    val first = blocker.first()
                    if (alongElements.contains(first.e)) {
                        // we can never traverse inside the intersection
                        return first.e
                    } else if (first.e is Diagram) {
                        return null
                    } else if (first.canCross(d)) {
                        return null
                    } else {
                        return first.e
                    }
                } else {
                    throw LogicException("What is this?")
                }
            } else {
                return null
            }
        }

        val allElements = so.getAllPositioned()

        so.getAllSlideables()
            .filter { it.getIntersectingElements().isNotEmpty() }
            .forEach { along ->
                // anything that is along the path of along
                val blockingElements = allElements
                    .filter { e ->
                        val set = so.getSlideablesFor(e)!!
                        intersects(along, set.l, set.r) }
                    .toSet()

                // slideables for the above elements
                val blockingSlideables = blockingElements
                    .map { e -> sox.getSlideablesFor(e)!!}
                    .flatMap { it -> listOf(it.l, it.r) }
                    .toSet()


                // actual intersecting elements
                val traversalOrder = (blockingSlideables +
                        sox.getAllSlideables().filter { it.getRectElements().isEmpty() })
                    .sortedBy { it.minimumPosition }


                var prev : C2Slideable? = null
                var blockedBy : DiagramElement? = null

                for(curr in traversalOrder) {
                    if (blockedBy == null) {
                        c2.addNeighbour(along, prev, curr)
                        prev = curr
                        blockedBy = canTraverse(curr, blockingSlideables, along.getIntersectingElements(), d)
                    } else {
                        prev = curr
                        blockedBy = canEmergeFrom(curr, blockedBy)
                    }
                }
            }
    }


    override val prefix: String
        get() = "NBCS"

    override val isLoggingEnabled: Boolean
        get() = true

}