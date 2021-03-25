package org.kite9.diagram.visualization.compaction.rect

import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
import org.kite9.diagram.common.elements.vertex.FanVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.isHorizontal
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.segment.Segment
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction.segment.SegmentSlackOptimisation
import org.kite9.diagram.visualization.compaction.segment.SegmentSlideable

/**
 * Stores the segments on the stack
 */
class VertexTurn(
    val number: Int,
    val compaction: Compaction,
    val slideable: ElementSlideable,
    val direction: Direction,
    var startsWith: ElementSlideable,
    var endsWith: ElementSlideable,
    val partOf: Rectangular?
) {
    enum class TurnPriority(val costFactor: Int) {
        MINIMIZE_RECTANGULAR(3), CONNECTION(1), MAXIMIZE_RECTANGULAR(0);
    }

    private var start: Vertex? = commonVertex(slideable, startsWith)
    private var end: Vertex? = commonVertex(slideable, endsWith)

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

    private fun commonVertex(a: ElementSlideable, b: ElementSlideable): Vertex? {
        return a.getVerticesOnSlideable().first { b.getVerticesOnSlideable().contains(it) }
    }


    private fun recalculateLength(): Double {
        val startUse: ElementSlideable? = checkMinimizeSlideable(
            startsWith, true
        )
        val endUse: ElementSlideable? = checkMinimizeSlideable(
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
        orig: ElementSlideable,
        from: Boolean
    ): ElementSlideable? {
        if (calculateTurnPriority(orig) != TurnPriority.MINIMIZE_RECTANGULAR) {
            return orig
        }
        val maxTo: Boolean = increasingDirection()
        val needed: Side = if ((maxTo == from)) Side.START else Side.END
        val current: Side? = orig.getSingleSide()
        if ((current === needed) || (current === Side.BOTH)) {
            return orig
        }
        val rects: Set<Rectangular> = orig.getRectangulars()
        val rect: Rectangular = getTopmostRectangular(rects)
        return (orig.so as SegmentSlackOptimisation).getSlideablesFor(rect).otherOne(orig)
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
        get() = (slideable as SegmentSlideable).underlying

    override fun toString(): String {
        return "[$number $turnPriority\n     s=$slideable\n  from=$startsWith\n    to=$endsWith\n     d=$direction\n]"
    }

    fun resetEndsWith(s: ElementSlideable, tp: TurnPriority, minLength: Double) {
        endsWith = s
        end = null
        turnPriority = TurnPriority.values()[tp.ordinal.coerceAtLeast(turnPriority.ordinal)]
        length = 0.0.coerceAtLeast(minLength)
    }

    fun resetStartsWith(s: ElementSlideable, tp: TurnPriority, minLength: Double) {
        startsWith = s
        start = null
        turnPriority = TurnPriority.values()[tp.ordinal.coerceAtLeast(turnPriority.ordinal)]
        length = 0.0.coerceAtLeast(minLength)
    }

    fun ensureMinLength(l: Double) {
        val early: ElementSlideable = if (increasingDirection()) startsWith else endsWith
        val late: ElementSlideable = if (increasingDirection()) endsWith else startsWith
        early.so.ensureMinimumDistance(early, late, l.toInt())
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
    private fun calculateTurnPriority(s: ElementSlideable): TurnPriority {
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
        val labels: Int = slideable.getUnderlyingInfo()
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

    fun isMinimizeRectangular(s: ElementSlideable): Boolean {
        val rects: Int = s.getUnderlyingInfo()
            .map { it.diagramElement }
            .filterIsInstance<Rectangular>()
            .filter(minimize(isHorizontal(direction))).count()
        return rects > 0
    }

    fun isMaximizeRectangular(s: ElementSlideable): Boolean {
        val rects: Int = s.getUnderlyingInfo()
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
        fun isConnection(s: ElementSlideable): Boolean {
            val connections: Int = s.getUnderlyingInfo()
                .map { it.diagramElement }
                .filterIsInstance<Connection>()
                .count()
            return connections > 0
        }
    }

}