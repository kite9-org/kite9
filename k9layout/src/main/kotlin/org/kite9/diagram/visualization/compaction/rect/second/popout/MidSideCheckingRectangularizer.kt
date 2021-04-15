package org.kite9.diagram.visualization.compaction.rect.second.popout

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.isHorizontal
import org.kite9.diagram.model.style.ConnectionAlignment
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction.rect.VertexTurn
import org.kite9.diagram.visualization.compaction.rect.second.prioritised.PrioritizingRectangularizer
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.orthogonalization.DartFace

/**
 * Does extra calculations of the [PrioritisedRectOption] to make sure that it will be
 * respecting middle-alignment of connections.
 */
abstract class MidSideCheckingRectangularizer(cd: CompleteDisplayer?) : PrioritizingRectangularizer(cd) {

    private fun shouldSetMidpoint(vt: VertexTurn, link: VertexTurn?): Boolean {
        val connecteds = getConnecteds(vt)
        if (connecteds.size == 1) {
            if (link == null || link.slideable.connections.size == 1) {
                val leavingConnections = vt.leavingConnections
                if (leavingConnections.size == 1) {
                    val theConnected = connecteds.iterator().next()
                    val theConnection = leavingConnections.iterator().next()
                    if (!theConnection.meets(theConnected)) {
                        // we should only do a mid-point if we're connecting to this element
                        return false
                    }
                    if (link == null || link.slideable.connections.containsAll(leavingConnections)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun getConnecteds(vt: VertexTurn): Set<ConnectedRectangular> {
        return vt.slideable.rectangulars
            .filterIsInstance<ConnectedRectangular>()
            .toSet()
    }

    private fun getRectangular(vt: VertexTurn): Rectangular? {
        val r = vt.slideable.rectangulars
        if (r.size == 2) {
            // label and container
            val i = r.iterator()
            val r1 = i.next()
            val r2 = i.next()
            return if (r1.getParent() === r2) {
                r2
            } else if (r2.getParent() === r1) {
                r1
            } else {
                throw LogicException()
            }
        } else if (r.size > 2) {
            throw LogicException()
        } else if (r.size == 0) {
            return null
        }
        return r.iterator().next()
    }

    /**
     * Sets up the mid-points as part of secondary sizing.
     */
    override fun performSecondarySizing(c: Compaction, stacks: Map<DartFace, MutableList<VertexTurn>>) {
        super.performSecondarySizing(c, stacks)
        stacks.values
            .flatMap { it }
            .filter { onlyAligned(it) }
            .distinct()
            .forEach { alignSingleConnections(c, it) }
    }

    protected fun alignSingleConnections(c: Compaction, vt: VertexTurn) {
        if (shouldSetMidpoint(vt, null)) {
            val underlying = vt.slideable.underlyingInfo
                .map { (diagramElement) -> diagramElement }
                .filterIsInstance<ConnectedRectangular>()
                .first()
            val out = alignSingleConnections(c, underlying, isHorizontal(vt.direction), false)
            if (out != null) {
                vt.ensureMinLength(out.midPoint.toDouble())
                vt.isNonExpandingLength = out.safe
            }
        }
    }

    companion object {
        protected fun onlyAligned(vt: VertexTurn): Boolean {
            return vt.slideable.underlyingInfo
                .filter { it.diagramElement is ConnectedRectangular }
                .filter { it.diagramElement is Container }
                .filter { (diagramElement, side) ->
                    (diagramElement as ConnectedRectangular).getConnectionAlignment(
                        getDirection(
                            side,
                            vt.direction
                        )
                    ) !== ConnectionAlignment.NONE
                }
                .filter { (diagramElement) ->
                    (matchesPattern(diagramElement as Container, vt.startsWith, vt.endsWith)
                            || matchesPattern(
                        diagramElement,
                        vt.endsWith,
                        vt.startsWith
                    ))
                }
                .count() > 0
        }

        private fun getDirection(side: Side, vtDirection: Direction): Direction {
            return when (vtDirection) {
                Direction.UP, Direction.DOWN -> if (side === Side.START) {
                    Direction.UP
                } else if (side === Side.END) {
                    Direction.DOWN
                } else {
                    throw LogicException()
                }
                Direction.LEFT, Direction.RIGHT -> if (side === Side.START) {
                    Direction.UP
                } else if (side === Side.END) {
                    Direction.DOWN
                } else {
                    throw LogicException()
                }
            }
            throw LogicException()
        }

        private fun matchesPattern(underlying: Container, underlyingEnd: ElementSlideable, connectionEnd: ElementSlideable): Boolean {
            return underlyingEnd.hasUnderlying(underlying) && connectionEnd.connections.size == 1
        }
    }
}