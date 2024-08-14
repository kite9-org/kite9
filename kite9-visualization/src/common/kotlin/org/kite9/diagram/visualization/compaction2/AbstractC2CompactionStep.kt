package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.SlideableSet
import org.kite9.diagram.visualization.display.CompleteDisplayer

abstract class AbstractC2CompactionStep(val cd: CompleteDisplayer) : C2CompactionStep, Logable {

    val log by lazy { Kite9Log.instance(this) }

    private fun toDirection(s: Side?, d: Dimension) : Direction? {
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
        aSide: Side?,
        b: DiagramElement,
        bSide: Side?,
        d: Dimension,
        along: DiagramElement?,
        concave: Boolean
    ) : Double {
        val d1 =toDirection(aSide, d)
        val d2 = toDirection(bSide, d)
        if ((d1 != null) && (d2 != null)) {
            return cd.getMinimumDistanceBetween(a, d1, b, d2, toDirection(d), along, concave)
        } else {
            return 0.0
        }
    }
    /**
     * Makes sure that the contents are within the container, with correct spacing either side.
     */
    fun embed(
        d: Dimension,
        container: RectangularSlideableSet,
        contents: SlideableSet<*>?,
        cso: C2SlackOptimisation,
        e: DiagramElement
    ) {
        log.send("Embedding $e inside $container.d")
        when (contents) {
            is RectangularSlideableSet -> {
                val distL = getMinimumDistanceBetween(container.d, Side.START, contents.d, Side.START, d, null, true)
                val distR = getMinimumDistanceBetween(container.d, Side.END, contents.d, Side.END, d, null, true)
                separateRectangular(container, Side.START, contents, Side.START, cso, distL)
                separateRectangular(contents, Side.END, container, Side.END, cso, distR)
            }
            is RoutableSlideableSet -> {
                val leftC = container.l
                val rightC = container.r
                val leftI = contents.bl
                val rightI = contents.br
                if (leftI != null) cso.ensureMinimumDistance(leftC, leftI, 0)
                if (rightI != null) cso.ensureMinimumDistance(rightI, rightC, 0)
            }
            null -> {
            }
            else -> throw LogicException("Unknown type")
        }
    }



    /**
     * Ensures that b is after a by a given distance
     */
    fun separateRectangular(
        a: SlideableSet<*>,
        aSide: Side,
        b: SlideableSet<*>,
        bSide: Side,
        cso: C2SlackOptimisation,
        dist: Double
    ) {
        val set1 = cso.getRectangularsOnSide(aSide, a)
        val set2 = cso.getRectangularsOnSide(bSide, b)
        set1.forEach { l ->
            set2.forEach { r ->  cso.ensureMinimumDistance(l, r, dist.toInt())}
        }
    }

    fun ensureCentreSlideablePosition(cso: C2SlackOptimisation, ss: RectangularSlideableSet, c: C2IntersectionSlideable?) {
        if (c != null) {
            val minDist = ss.l.minimumDistanceTo(ss.r)
            cso.ensureMinimumDistance(ss.l, c, (minDist / 2.0).toInt())
            cso.ensureMinimumDistance(c, ss.r, (minDist / 2.0).toInt())
        }
    }

    fun visitRectangulars(r: Rectangular, f: (r: Rectangular) -> Unit) {
        if (r is Container) {
            r.getContents()
                .filterIsInstance<Rectangular>()
                .forEach { visitRectangulars(it, f) }
        }

        f(r)
    }


}