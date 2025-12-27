package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

/**
 * This makes sure that any time we have all the groups to complete a container, we wrap the groups in the
 * container(s) and use that instead.
 */
abstract class AbstractC2ContainerCompactionStep(cd: CompleteDisplayer, r: GroupResult, val rr: RoutableReader) : AbstractC2BuilderCompactionStep(cd) {

    fun bucketInDimension(c: Container, horiz: Boolean) : List<Set<DiagramElement>> {
        val contents = c.getContents().filterIsInstance<Connected>()
        val buckets = mutableListOf<MutableSet<DiagramElement>>()
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
                val newB = mutableSetOf<DiagramElement>(e)
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

    fun applyContainerEdge(so: C2SlackOptimisation, c: Container, to: Set<DiagramElement>, s: Side, gm: Map<DiagramElement, LeafGroup>, dimension: Dimension, topGroup: Group) {
        to.forEach {
            val lg = gm.get(it)
            if (lg != null) {
                val routables = so.getSlideablesFor(lg)
                if (routables.isNotEmpty()) {
                    val theRSS = routables.last()
                    val inside = checkCreateElement(c, dimension, so, null, topGroup)
                    so.addSide(inside, theRSS, s)
                    if (s == Side.START) {
                        if (theRSS.bl != null) {
                            so.ensureMinimumDistance(inside.l, theRSS.bl!!, 0)
                        }
                    } else {
                        if (theRSS.br != null) {
                            so.ensureMinimumDistance(theRSS.br!!, inside.r, 0)
                        }
                    }
                } else {
                    // throw LogicException("No routables for element")
                    // do nothing.
                }
            }
        }
    }

    private fun completeContainers(c: Container, co: C2Compaction, gm: Map<DiagramElement, LeafGroup>, topGroup: Group) {
        println("Completing ${c}")
        val pp = rr.getPlacedPosition(c)
        if (pp == null) {

            c.getContents()
                .filterIsInstance<Container>()
                .forEach { completeContainers(it, co, gm, topGroup) }

            val xd = bucketInDimension(c, true)
            val yd = bucketInDimension(c, false)

            if ((xd.isNotEmpty()) && (yd.isNotEmpty())) {

                val leftMost = xd.first()
                val rightMost = xd.last()
                val topMost = yd.first()
                val bottomMost = yd.last()

                val sox = co.getSlackOptimisation(Dimension.H)
                val soy = co.getSlackOptimisation(Dimension.V)

                applyContainerEdge(sox, c, leftMost, Side.START, gm, Dimension.H, topGroup)
                applyContainerEdge(sox, c, rightMost, Side.END, gm, Dimension.H, topGroup)
                applyContainerEdge(soy, c, topMost, Side.START, gm, Dimension.V, topGroup)
                applyContainerEdge(soy, c, bottomMost, Side.END, gm, Dimension.V, topGroup)

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

    private fun ensurePositionsOfElements(mapping: Map<DiagramElement, LeafGroup>) {
        mapping.forEach { (k, v) ->
            val pos = rr.getPlacedPosition(v)
            (rr as RoutableHandler2D).setPlacedPosition(k, pos!!)
        }
    }

    fun wrapContainersIntoGroups(c2: C2Compaction, topGroup: Group) {
        val mapping = relevantElements(topGroup)
        ensurePositionsOfElements(mapping)
        val dia = c2.getDiagram()
        completeContainers(dia, c2, mapping, topGroup)
    }

    private fun <X, Y> mergeMaps(a: Map<X, Y>, b: Map<X, Y>) : Map<X, Y> {
        return (a.keys + b.keys).associateWith {
            val aVal = a[it] ?: null
            val bVal = b[it] ?: null
            if ((aVal != null) && (bVal != null) && (aVal != bVal)) {
                throw LogicException("Should be only one leaf group for each element")
            } else aVal ?: bVal!!
        }
    }

    private fun relevantElements(topGroup: Group) : Map<DiagramElement, LeafGroup> {
        return when (topGroup) {
            is CompoundGroup -> mergeMaps(relevantElements(topGroup.a),(relevantElements(topGroup.b)))
            is LeafGroup -> {
                val c = topGroup.connected
                if (c != null) {
                    mapOf(c to topGroup)
                } else {
                    mapOf()
                }
            }
        }
    }

    fun horizontalAxis(g: Group): Boolean {
        return g.axis.isHorizontal || g is LeafGroup
    }

    fun verticalAxis(g: Group): Boolean {
        return g.axis.isVertical || g is LeafGroup
    }

}