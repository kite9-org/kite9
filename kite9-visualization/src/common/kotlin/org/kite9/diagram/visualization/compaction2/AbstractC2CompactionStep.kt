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

    /**
     * Makes sure that the contents are within the container, with correct spacing either side.
     */
    fun embed(
        d: Dimension,
        container: RectangularSlideableSet,
        contents: RectangularSlideableSet?,
        cso: C2SlackOptimisation
    ) {
        if (contents != null) {
            val distL = getMinimumDistanceBetween(container.d, Side.START, contents.d, Side.START, d, null, true)
            val distR = getMinimumDistanceBetween(container.d, Side.END, contents.d, Side.END, d, null, true)
            separateRectangular(container, Side.START, contents, Side.START, cso, distL)
            separateRectangular(contents, Side.END, container, Side.END, cso, distR)
        }
    }

    /**
     * Ensures that b is after a by a given distance
     */
    fun separateRectangular(
        a: RectangularSlideableSet,
        aSide: Side,
        b: RectangularSlideableSet,
        bSide: Side,
        cso: C2SlackOptimisation,
        dist: Double
    ) {
        cso.ensureMinimumDistance(a.getRectangularOnSide(aSide), b.getRectangularOnSide(bSide), dist.toInt())
    }
}