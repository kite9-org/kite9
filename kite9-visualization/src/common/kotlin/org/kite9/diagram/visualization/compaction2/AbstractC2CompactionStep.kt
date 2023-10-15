package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.display.CompleteDisplayer

abstract class AbstractC2CompactionStep(val cd: CompleteDisplayer) : C2CompactionStep, Logable {

    val log by lazy { Kite9Log.instance(this) }

    private fun toDirection(s: Side, d: Dimension) : Direction {
        return if (d == Dimension.H) {
            if (s ==Side.START)
                Direction.LEFT
            else
                Direction.RIGHT
        } else {
            if (s ==Side.START)
                Direction.UP
            else
                Direction.DOWN
        }
    }

    private fun toDirection(d: Dimension) : Direction {
        return if (d == Dimension.H) Direction.RIGHT else Direction.DOWN
    }

    fun getMinimumDistanceBetween(
        a: DiagramElement,
        aSide: Side,
        b: DiagramElement,
        bSide: Side,
        d: Dimension,
        along: DiagramElement?,
        concave: Boolean
    ) : Double = cd.getMinimumDistanceBetween(a, toDirection(aSide, d), b, toDirection(bSide, d), toDirection(d), along, concave)

}