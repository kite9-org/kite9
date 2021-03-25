package org.kite9.diagram.visualization.compaction.segment

import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.DirectionEnforcingElement
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction.UnderlyingInfo
import org.kite9.diagram.visualization.display.CompleteDisplayer
import kotlin.math.max

class SegmentSlideable(
    so: SegmentSlackOptimisation,
    val underlying: Segment
) : ElementSlideable(so) {

    override fun toString(): String {
        return "<" + (so as SegmentSlackOptimisation).getIdentifier(underlying) + " " + minimum.position + "," + maximum.position + ">"
    }

    override fun getMinimumDistancePossible(
        to: ElementSlideable,
        along: ElementSlideable?,
        concave: Boolean,
        displayer: CompleteDisplayer
    ): Double {
        return getMinimumDistance((this as SegmentSlideable).underlying,
            (to as SegmentSlideable).underlying,
            (along as? SegmentSlideable)?.underlying,
            concave,
            displayer);
    }

    override fun getAdjoiningSlideables(c: Compaction): List<ElementSlideable> {
        return underlying.getAdjoiningSegments(c)
            .map { it.slideable!! }
    }

    override fun getVerticesOnSlideable(): Set<Vertex> {
        return underlying.getVerticesInSegment()
    }

    override fun getConnections(): Set<Connection> {
        return underlying.connections
    }

    override fun hasUnderlying(c: Container): Boolean {
        return underlying.hasUnderlying(c)
    }

    override fun getUnderlyingInfo(): Set<UnderlyingInfo> {
        return underlying.underlyingInfo
    }

    override fun getSingleSide(): Side? {
        return underlying.singleSide
    }

    override fun getRectangulars(): Set<Rectangular> {
        return underlying.rectangulars
    }

    private fun getMinimumDistance(first: Segment, second: Segment, along: Segment?, concave: Boolean, displayer: CompleteDisplayer): Double {
        val horizontalDartFirst = first.dimension === Dimension.V
        val horizontalDartSecond = second.dimension === Dimension.V
        if (horizontalDartFirst != horizontalDartSecond) {
            throw LogicException()
        }
        if (first.underlyingInfo.size > 1 && second.underlyingInfo.size > 1) {
            // we're in a grid, look for common diagram elements
            val combined: MutableSet<Rectangular> = HashSet(first.rectangulars)
            val secondRs = second.rectangulars
            combined.retainAll(secondRs)
            if (combined.size == 1) {
                // ok, run just the single found combination
                val max = 0.0
                for (fromUI in first.underlyingInfo) {
                    if (combined.contains(fromUI.diagramElement)) {
                        return getMinimumDistance(horizontalDartFirst, fromUI, second, along, concave, displayer)
                    }
                }
                throw LogicException()
            }
        }

        // ok, run all the combinations
        var max = 0.0
        for (fromUI in first.underlyingInfo) {
            max = max(max, getMinimumDistance(horizontalDartFirst, fromUI, second, along, concave, displayer))
        }
        return max
    }

    private fun getMinimumDistance(
        horizontalDart: Boolean,
        fromUI: UnderlyingInfo,
        second: Segment,
        along: Segment?,
        concave: Boolean,
        displayer: CompleteDisplayer
    ): Double {
        var max = 0.0
        for (toUI in second.underlyingInfo) {
            max = max(max, getMinimumDistance(horizontalDart, fromUI, toUI, along, concave, displayer))
        }
        return max
    }

    private fun getMinimumDistance(
        horizontalDart: Boolean,
        fromUI: UnderlyingInfo,
        toUI: UnderlyingInfo,
        along: Segment?,
        concave: Boolean,
        displayer: CompleteDisplayer
    ): Double {
        val fromde = fromUI.diagramElement
        val fromUnderlyingSide = convertSideToDirection(horizontalDart, fromUI.side, true)
        val tode = toUI.diagramElement
        val toUnderlyingSide = convertSideToDirection(horizontalDart, toUI.side, false)
        if (!needsLength(fromde, tode)) {
            return 0.0
        }
        val alongDe = getAlongDiagramElement(along)
        return displayer.getMinimumDistanceBetween(
            fromde,
            fromUnderlyingSide,
            tode,
            toUnderlyingSide,
            if (horizontalDart) Direction.RIGHT else Direction.DOWN,
            alongDe,
            concave
        )
    }

    private fun needsLength(a: DiagramElement, b: DiagramElement): Boolean {
        return if (a is DirectionEnforcingElement || b is DirectionEnforcingElement) {
            false
        } else true
    }

    private fun convertSideToDirection(horizontalDart: Boolean, side: Side, first: Boolean): Direction {
        return when (side) {
            Side.END -> if (horizontalDart) Direction.RIGHT else Direction.DOWN
            Side.START -> if (horizontalDart) Direction.LEFT else Direction.UP
            else -> if (horizontalDart) {
                if (first) Direction.RIGHT else Direction.LEFT
            } else {
                if (first) Direction.DOWN else Direction.UP
            }
        }
    }

    private fun getAlongDiagramElement(along: Segment?): DiagramElement? {
        return if (along == null) {
            null
        } else along.getUnderlyingWithSide(Side.NEITHER)
            ?: return along.underlyingInfo
                .map { (diagramElement) -> diagramElement }
                .firstOrNull()
    }
}