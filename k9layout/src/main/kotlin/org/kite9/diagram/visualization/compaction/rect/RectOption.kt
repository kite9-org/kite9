package org.kite9.diagram.visualization.compaction.rect

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse

open class RectOption(
    protected val i: Int,
    val vt1: VertexTurn,
    val vt2: VertexTurn,
    val vt3: VertexTurn,
    val vt4: VertexTurn,
    val vt5: VertexTurn,
    val match: PrioritizingRectangularizer.Match,
    val stack: MutableList<VertexTurn>
) : Comparable<RectOption> {

    val meets: VertexTurn
        get() = if (match == PrioritizingRectangularizer.Match.A) vt4 else vt2
    val extender: VertexTurn
        get() = if (match == PrioritizingRectangularizer.Match.A) vt1 else vt5
    val par: VertexTurn
        get() = if (match == PrioritizingRectangularizer.Match.A) vt2 else vt4
    val link: VertexTurn
        get() = if (match == PrioritizingRectangularizer.Match.A) vt3 else vt3
    val post: VertexTurn
        get() = if (match == PrioritizingRectangularizer.Match.A) vt5 else vt1

    open var initialScore = 0;

    open fun calculateScore(): Int = 0

    open fun rescore() {
        initialScore = calculateScore()
    }

    override fun compareTo(o: RectOption): Int {
        val out = initialScore.compareTo(o.initialScore)
        return if (out != 0) {
            out
        } else i.compareTo(o.i)
    }

    override fun toString(): String {
        return "[RO: (" + initialScore + ") extender = " + extender.slideable + "]"
    }

    fun getTurnDirection(vt: VertexTurn): Direction? {
        return if (match == PrioritizingRectangularizer.Match.A) {
            vt.direction
        } else reverse(vt.direction)
    }
}