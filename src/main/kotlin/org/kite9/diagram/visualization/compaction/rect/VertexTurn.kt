package org.kite9.diagram.visualization.compaction.rect

import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.vertex.FanVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.isHorizontal
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.segment.Segment
import org.kite9.diagram.visualization.compaction.segment.Side
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation

/**
 * Stores the segments on the stack
 */
internal class VertexTurn(
    val number: Int,
    val compaction: Compaction,
    val slideable: Slideable<Segment>,
    val direction: Direction,
    var startsWith: Slideable<Segment>,
    var endsWith: Slideable<Segment>,
    val partOf: Rectangular?
) {
    internal enum class TurnPriority(val costFactor: Int) {
        MINIMIZE_RECTANGULAR(3), CONNECTION(1), MAXIMIZE_RECTANGULAR(0);
    }

    private var start: Vertex? = commonVertex(slideable.underlying, startsWith.underlying)
    private var end: Vertex? = commonVertex(slideable.underlying, endsWith.underlying)

    var turnPriority: TurnPriority = calculateTurnPriority(slideable)
        private set

    private var length: Double = 0.0

    /**
     * Returns true if the turn in question cannot be expanded due to a rectangle getting bigger
     * Questionable usefulness.
     * @return
     */
    var isNonExpandingLength: Boolean = false

    /**
     * getLength now considers that some elements need to be minimized.  If it starts/ends with a
     * MINIMIZE section, it will calculate to the other end of this
     * @param recalculate
     * @return
     */
    fun getLength(recalculate: Boolean): Double {
        if (recalculate) {
            val newValue: Double = recalculateLength()
            if (newValue != length) {
                //if (fixedLength) {
                //	throw new LogicException("Length shouldn't change!");
                //}
                length = newValue
            }
        }
        return length
    }

    private fun commonVertex(a: Segment, b: Segment): Vertex? {
        return a.getVerticesInSegment().first { b.getVerticesInSegment().contains(it) }
    }


    private fun recalculateLength(): Double {
        val startUse: Slideable<Segment>? = checkMinimizeSlideable(
            startsWith, true
        )
        val endUse: Slideable<Segment>? = checkMinimizeSlideable(
            endsWith, false
        )
        val increasing: Boolean = increasingDirection()
        val mainDistance: Double = if (increasing) startUse!!.minimumDistanceTo((endUse)!!)
            .toDouble() else endUse!!.minimumDistanceTo((startUse)!!).toDouble()
        var earlyReduction: Double = 0.0
        var lateReduction: Double = 0.0
        if (startUse != startsWith) {
            earlyReduction =
                if (increasing) startUse.minimumDistanceTo(startsWith).toDouble() else startsWith.minimumDistanceTo(
                    (startUse)
                ).toDouble()
        }
        if (endUse != endsWith) {
            lateReduction =
                if (increasing) endsWith.minimumDistanceTo((endUse)).toDouble() else endUse.minimumDistanceTo(
                    endsWith
                ).toDouble()
        }
        return mainDistance - earlyReduction - lateReduction
    }

    /**
     * When given a vertexTurn which is a subset of a whole length, this can extend the
     * length to the whole slideable in order that we can calculate a length based on the whole thing (?)
     */
    private fun checkMinimizeSlideable(
        orig: Slideable<Segment>,
        from: Boolean
    ): Slideable<Segment>? {
        if (calculateTurnPriority(orig) != TurnPriority.MINIMIZE_RECTANGULAR) {
            return orig
        }
        val maxTo: Boolean = increasingDirection()
        val needed: Side = if ((maxTo == from)) Side.START else Side.END
        val current: Side? = orig.underlying.singleSide
        if ((current === needed) || (current === Side.BOTH)) {
            return orig
        }
        val rects: Set<Rectangular> = orig.underlying.rectangulars
        val rect: Rectangular = getTopmostRectangular(rects)
        return (orig.slackOptimisation as SegmentSlackOptimisation).getSlideablesFor(rect).otherOne(orig)
    }

    private fun getTopmostRectangular(rects: Set<Rectangular>): Rectangular {
        if (rects.size == 0) {
            throw LogicException("Couldn't determine underlying")
        }
        return rects.reduce { r1: Rectangular, r2: Rectangular ->
            if (r1.getDepth() > r2.getDepth()) {
                return@reduce r2
            } else if (r2.getDepth() > r1.getDepth()) {
                return@reduce r1
            } else {
                return@reduce r1
            }
        }
    }

    fun increasingDirection(): Boolean {
        return (direction === Direction.DOWN) || (direction === Direction.RIGHT)
    }

    val segment: Segment
        get() = slideable.underlying

    override fun toString(): String {
        return "[$number $turnPriority\n     s=$slideable\n  from=$startsWith\n    to=$endsWith\n     d=$direction\n]"
    }

    fun resetEndsWith(s: Slideable<Segment>, tp: TurnPriority, minLength: Double) {
        endsWith = s
        end = null
        turnPriority = TurnPriority.values()[tp.ordinal.coerceAtLeast(turnPriority.ordinal)]
        length = 0.0.coerceAtLeast(minLength)
    }

    fun resetStartsWith(s: Slideable<Segment>, tp: TurnPriority, minLength: Double) {
        startsWith = s
        start = null
        turnPriority = TurnPriority.values()[tp.ordinal.coerceAtLeast(turnPriority.ordinal)]
        length = 0.0.coerceAtLeast(minLength)
    }

    fun ensureMinLength(l: Double) {
        val early: Slideable<Segment> = if (increasingDirection()) startsWith else endsWith
        val late: Slideable<Segment> = if (increasingDirection()) endsWith else startsWith
        early.slackOptimisation.ensureMinimumDistance(early, late, l.toInt())
        length = l
    }

    fun isFanTurn(atEnd: VertexTurn): Boolean {
        if (isFanTurn) {
            if (atEnd.slideable == startsWith) {
                return isStartInnerFan
            } else if (atEnd.slideable == endsWith) {
                return isEndInnerFan
            } else {
                throw LogicException()
            }
        }
        return false
    }

    /**
     * The order of deciding this is important, since a segment
     * can be shared by a label.
     */
    private fun calculateTurnPriority(s: Slideable<Segment>): TurnPriority {
        if (isConnection(s)) {
            return TurnPriority.CONNECTION
        } else if (isMaximizeRectangular(s)) {
            return TurnPriority.MAXIMIZE_RECTANGULAR
        } else if (isMinimizeRectangular(s)) {
            return TurnPriority.MINIMIZE_RECTANGULAR
        } else {
            return TurnPriority.CONNECTION // layout connection?
        }
    }

    private val isFanTurn: Boolean
        private get() = (start is FanVertex) && (end is FanVertex)

    val innerFanVertex: FanVertex?
        get() {
            if (isEndInnerFan) {
                return end as FanVertex?
            } else if (isStartInnerFan) {
                return start as FanVertex?
            } else {
                return null
            }
        }
    private val isEndInnerFan: Boolean
        private get() = (end is FanVertex) && (end as FanVertex).isInner
    private val isStartInnerFan: Boolean
        private get() = (start is FanVertex) && (start as FanVertex).isInner

    fun isContainerLabelOnSide(d: Direction?): Boolean {
        val labels: Int = slideable.underlying.underlyingInfo
            .map { it.diagramElement }
            .filterIsInstance<Label>()
            .filter { l: Label -> !l.isConnectionLabel() }
            .filter { l: Label ->
                l.getLabelPlacement()!!
                    .sameAxis(d)
            }
            .count()
        return (labels > 0)
    }

    private fun minimize(horiz: Boolean): (Rectangular) -> Boolean {
        return { r: Rectangular -> (r is SizedRectangular) && (r.getSizing(horiz) === DiagramElementSizing.MINIMIZE) && (r !== partOf) }
    }

    private fun maximize(horiz: Boolean): (Rectangular) -> Boolean {
        return { r: Rectangular -> ((r is SizedRectangular) && (r.getSizing(horiz) === DiagramElementSizing.MAXIMIZE)) || (r === partOf) }
    }

    fun isMinimizeRectangular(s: Slideable<Segment>): Boolean {
        val rects: Int = s.underlying.underlyingInfo
            .map { it.diagramElement }
            .filterIsInstance<Rectangular>()
            .filter(minimize(isHorizontal(direction))).count()
        return rects > 0
    }

    fun isMaximizeRectangular(s: Slideable<Segment>): Boolean {
        val rects: Int = s.underlying.underlyingInfo
            .map { it.diagramElement }
            .filterIsInstance<Rectangular>()
            .filter(maximize(isHorizontal(direction))).count()
        return rects > 0
    }

    val leavingConnections: Set<Connection>
        get() {
            val leavingConnections: Set<Connection> = segment.getAdjoiningSegments(compaction)
                .flatMap { it.connections }
                .toSet()
            return leavingConnections
        }

    companion object {
        fun isConnection(s: Slideable<Segment>): Boolean {
            val connections: Int = s.underlying.underlyingInfo
                .map { it.diagramElement }
                .filterIsInstance<Connection>()
                .count()
            return connections > 0
        }
    }

}