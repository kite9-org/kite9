package org.kite9.diagram.visualization.compaction.rect

import org.kite9.diagram.common.algorithms.ssp.PriorityQueue
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.isHorizontal
import org.kite9.diagram.model.style.ConnectionAlignment
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.rect.VertexTurn.TurnPriority
import org.kite9.diagram.visualization.compaction.segment.Segment
import org.kite9.diagram.visualization.compaction.segment.Side
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.orthogonalization.DartFace

/**
 * Does extra calculations of the [PrioritisedRectOption] to make sure that it will be
 * respecting middle-alignment of connections.
 */
abstract class MidSideCheckingRectangularizer(cd: CompleteDisplayer?) : PrioritizingRectangularizer(cd) {
    /**
     * If we have a 'safe' rectangularization, make sure meets can't increase
     */
    override fun checkRectOptionIsOk(
        onStack: Set<VertexTurn>,
        ro: RectOption,
        pq: PriorityQueue<RectOption>,
        c: Compaction
    ): Action {
        val superAction = super.checkRectOptionIsOk(onStack, ro, pq, c)
        if (superAction !== Action.OK) {
            return superAction
        }
        log.send("Checking: $ro")
        val turnDirection = ro.getTurnDirection(ro.extender)
        log.send("Extender: " + ro.extender + " dir= " + turnDirection)
        val meets = ro.meets
        val link = ro.link
        val par = ro.par
        val meetsMinimumLength = checkMinimumLength(meets, link, c)
        val parMinimumLength = checkMinimumLength(par, link, c)
        if (ro.calculateScore() != ro.initialScore) {
            // change it and throw it back in - priority has changed.
            log.send("Deferring: $meetsMinimumLength for meets=$meets\n         $parMinimumLength for par=$par")
            return Action.PUT_BACK
        }
        return Action.OK
        //		log.send("Allowing: meets="+ro.getMeets()+"\n          for par="+ro.getPar());						
    }

    private fun checkMinimumLength(rect: VertexTurn, link: VertexTurn, c: Compaction): Int {
        if (rect.turnPriority === TurnPriority.MINIMIZE_RECTANGULAR) {
            if (shouldSetMidpoint(rect, link)) {

                // ok, size is needed of overall rectangle then half.
                val r = getRectangular(rect)
                val isHorizontal = !isHorizontal(rect.direction)
                val along =
                    (if (isHorizontal) c.getHorizontalSegmentSlackOptimisation() else c.getVerticalSegmentSlackOptimisation())
                        .getSlideablesFor(r!!)
                val perp =
                    (if (!isHorizontal) c.getHorizontalSegmentSlackOptimisation() else c.getVerticalSegmentSlackOptimisation())
                        .getSlideablesFor(r)
                alignSingleConnections(c, perp, along, false, true)
            }
        }
        return rect.getLength(true).toInt()
    }

    private fun shouldSetMidpoint(vt: VertexTurn, link: VertexTurn?): Boolean {
        val connecteds = getConnecteds(vt)
        if (connecteds.size == 1) {
            if (link == null || link.segment.connections.size == 1) {
                val leavingConnections = vt.leavingConnections
                if (leavingConnections.size == 1) {
                    val theConnected = connecteds.iterator().next()
                    val theConnection = leavingConnections.iterator().next()
                    if (!theConnection.meets(theConnected)) {
                        // we should only do a mid-point if we're connecting to this element
                        return false
                    }
                    if (link == null || link.segment.connections.containsAll(leavingConnections)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun getConnecteds(vt: VertexTurn): Set<Connected> {
        return vt.segment.rectangulars
            .filterIsInstance<Connected>()
            .toSet()
    }

    private fun getRectangular(vt: VertexTurn): Rectangular? {
        val r = vt.segment.rectangulars
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
            val underlying = vt.segment.underlyingInfo
                .map { (diagramElement) -> diagramElement }
                .filterIsInstance<Connected>()
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
            return vt.segment.underlyingInfo
                .filter { it.diagramElement is Connected }
                .filter { it.diagramElement is Container }
                .filter { (diagramElement, side) ->
                    (diagramElement as Connected).getConnectionAlignment(
                        getDirection(
                            side,
                            vt.direction
                        )
                    ) !== ConnectionAlignment.NONE
                }
                .filter { (diagramElement) ->
                    (matchesPattern(diagramElement as Container, vt.startsWith.underlying, vt.endsWith.underlying)
                            || matchesPattern(
                        diagramElement,
                        vt.endsWith.underlying,
                        vt.startsWith.underlying
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

        private fun matchesPattern(underlying: Container, underlyingEnd: Segment, connectionEnd: Segment): Boolean {
            return underlyingEnd.hasUnderlying(underlying) && connectionEnd.connections.size == 1
        }
    }
}