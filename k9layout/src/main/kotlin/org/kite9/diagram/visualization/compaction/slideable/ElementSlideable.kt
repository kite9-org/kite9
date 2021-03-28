/**
 *
 */
package org.kite9.diagram.visualization.compaction.slideable

import org.kite9.diagram.common.algorithms.so.AlignStyle
import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.DirectionEnforcingElement
import org.kite9.diagram.common.elements.vertex.FanVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.VerticalAlignment
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction.UnderlyingInfo
import org.kite9.diagram.visualization.compaction.segment.SegmentSlideable
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.orthogonalization.Dart
import kotlin.math.max

/**
 * Extra functions for where a slideable represents a diagram element.
 */
open abstract class ElementSlideable(
    so: SlackOptimisation,
    val dimension: Dimension,
    val number: Int): Slideable(so) {

    abstract val verticesOnSlideable: Set<Vertex>

    abstract val underlyingInfo: Set<UnderlyingInfo>

    val singleSide: Side? by lazy {
        underlyingInfo
            .map { ui: UnderlyingInfo -> ui.side }
            .reduceRightOrNull { a: Side?, b: Side? -> sideReduce(a, b) }
    }

    private fun sideReduce(a: Side?, b: Side?): Side? {
        return if (a == null) {
            b
        } else if (b == null) {
            a
        } else if (a == b) {
            a
        } else {
            Side.BOTH
        }
    }

    val alignStyle : AlignStyle? by lazy {

        fun filterFanDirections(s: ElementSlideable, axis: (Direction) -> Boolean): Set<Direction> {
            return s.verticesOnSlideable
                .filterIsInstance<FanVertex>()
                .flatMap { it.fanSides }
                .filter { axis (it) }
                .toSet()
        }

        fun decideRectangularAlignStyle(s: ElementSlideable, de: AlignedRectangular): AlignStyle? {
            val des = if (de is SizedRectangular) (de as SizedRectangular).getSizing(s.dimension === Dimension.H) else null
            if (des === DiagramElementSizing.MINIMIZE || des == null) {
                if (s.dimension === Dimension.H) {
                    val va = de.getVerticalAlignment()
                    return when (va) {
                        VerticalAlignment.BOTTOM -> AlignStyle.MAX
                        VerticalAlignment.CENTER -> AlignStyle.CENTER
                        VerticalAlignment.TOP -> AlignStyle.MIN
                    }
                } else if (s.dimension === Dimension.V) {
                    val ha = de.getHorizontalAlignment()
                    return when (ha) {
                        HorizontalAlignment.LEFT -> AlignStyle.MIN
                        HorizontalAlignment.CENTER -> AlignStyle.CENTER
                        HorizontalAlignment.RIGHT -> AlignStyle.MAX
                    }
                }
            }
            return null
        }

        fun decideConnectionSegmentAlignStyle(s: ElementSlideable, de: Connection): AlignStyle? {
            if (de.getRenderingInformation().isContradicting) {
                return null
            }
            return if (s.dimension === Dimension.H) {
                // horizontal segment, push up or down
                val pushDirections = filterFanDirections(s) { Direction.isVertical( it ) }

//			if (pushDirections.size() > 1) {
//				throw new Kite9ProcessingException();
//			}
                if (pushDirections.size == 1) {
                    for (d in pushDirections) {
                        when (d) {
                            Direction.UP -> return AlignStyle.MIN
                            Direction.DOWN -> return AlignStyle.MAX
                            else -> {
                            }
                        }
                    }
                }
                AlignStyle.CENTER
            } else if (s.dimension === Dimension.V) {
                val pushDirections = filterFanDirections(s) { Direction.isHorizontal(it) }

//			if (pushDirections.size() > 1) {
//				throw new Kite9ProcessingException();
//			}
                if (pushDirections.size == 1) {
                    for (d in pushDirections) {
                        when (d) {
                            Direction.LEFT -> return AlignStyle.MIN
                            Direction.RIGHT -> return AlignStyle.MAX
                            else -> {
                            }
                        }
                    }
                }
                AlignStyle.CENTER
            } else {
                throw LogicException("No dimension on segment")
            }
        }


        val s = this
        val conns = s.connections
        if (conns.size == 1) {
            val de = conns.iterator().next()
            decideConnectionSegmentAlignStyle(s, de)
        } else if (conns.size == 0) {
            val toUse : AlignedRectangular? = s.underlyingInfo
                .map { it.diagramElement }
                .filterIsInstance<AlignedRectangular>()
                .sortedWith  { a : AlignedRectangular, b: AlignedRectangular -> a.getDepth().compareTo(b.getDepth()) }
                .firstOrNull()
            if (toUse != null) {
                decideRectangularAlignStyle(s, toUse)
            } else {
                null
            }
        } else {
            throw LogicException()
        }
    }


    open val rectangulars: Set<Rectangular> by lazy {
        underlyingInfo
            .map { it.diagramElement }
            .filterIsInstance<Rectangular>()
            .toSet()
    }

    open val connections: Set<Connection> by lazy {
        underlyingInfo
            .map { it.diagramElement }
            .filter { it is Connection }
            .map { it as Connection }
            .toSet()
    }

    fun getUnderlyingWithSide(s: Side): DiagramElement? {
        return underlyingInfo
            .filter { it.side == s }
            .map { it.diagramElement }
            .firstOrNull()
    }

    fun hasUnderlying(de: DiagramElement): Boolean {
        return underlyingInfo
            .map { u: UnderlyingInfo -> u.diagramElement }
            .filter { a: DiagramElement -> a === de }
            .count() > 0
    }

    fun hasUnderlying(des: Set<DiagramElement>): Boolean {
        return underlyingInfo
            .map { u: UnderlyingInfo -> u.diagramElement }
            .filter { a: DiagramElement -> des.contains(a) }
            .count() > 0
    }

    /**
     * Used for deciding whether a segment should be pushed left/right/up/down according to it's connections.
     */
    val adjoiningSegmentBalance: Int by lazy {
        val isHorizontal = dimension === Dimension.H
        verticesOnSlideable
            .map { v: Vertex ->
                if (isHorizontal) dartCount(v, Direction.DOWN) - dartCount(
                    v,
                    Direction.UP
                ) else dartCount(v, Direction.RIGHT) - dartCount(v, Direction.LEFT)
            }
            .sum()
    }

    private fun dartCount(v: Vertex, d: Direction): Int {
        for (e in v.getEdges()) {
            if (e is Dart && e.getDrawDirectionFrom(v) === d) {
                return 1
            }
        }
        return 0
    }


    private var leavingSegments: Set<ElementSlideable>? = null

    fun getAdjoiningSlideables(c: Compaction): Set<ElementSlideable> {
        if (leavingSegments == null) {
            val isHorizontal = dimension === Dimension.H

            // find segments that meet this one
            leavingSegments = verticesOnSlideable
                .map {
                    if (isHorizontal) c.getVerticalVertexSegmentMap()[it] else c.getHorizontalVertexSegmentMap()[it]
                }
                .filterNotNull()
                .toSet()
        }

        return leavingSegments!!
    }

    /**
     * Works out the distance to another slideable, taking into account margins/padding etc.  If concave is set true, it means that this
     * slideable and the parallel to slideable run alongside one another.
     */
    fun getMinimumDistance(second: ElementSlideable, along: ElementSlideable?, concave: Boolean, displayer: CompleteDisplayer): Double {

        fun convertSideToDirection(horizontalDart: Boolean, side: Side, first: Boolean): Direction {
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

        fun needsLength(a: DiagramElement, b: DiagramElement): Boolean {
            return if (a is DirectionEnforcingElement || b is DirectionEnforcingElement) {
                false
            } else true
        }

        fun startsAndEnds(uis: Set<UnderlyingInfo>) : Boolean {
            if (uis.size < 2) {
                return false;
            }
            var starts = false;
            var ends = false;
            uis.forEach {
                starts = starts || it.side == Side.START
                ends = ends || it.side == Side.END
            }

            return starts && ends
        }

        fun getAlongDiagramElement(along: ElementSlideable?): DiagramElement? {
            return if (along == null) {
                null
            } else along.getUnderlyingWithSide(Side.NEITHER)
                ?: return along.underlyingInfo
                    .map { (diagramElement) -> diagramElement }
                    .firstOrNull()
        }
        fun getMinimumDistanceInner(
            horizontalDart: Boolean,
            fromUI: UnderlyingInfo,
            toUI: UnderlyingInfo,
            along: ElementSlideable?,
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

        fun getMinimumDistance(
            horizontalDart: Boolean,
            fromUI: UnderlyingInfo,
            second: ElementSlideable,
            along: ElementSlideable?,
            concave: Boolean,
            displayer: CompleteDisplayer
        ): Double {
            var max = 0.0
            for (toUI in second.underlyingInfo) {
                max = max(max, getMinimumDistanceInner(horizontalDart, fromUI, toUI, along, concave, displayer))
            }
            return max
        }

        val horizontalDartFirst = this.dimension === Dimension.V
        val horizontalDartSecond = second.dimension === Dimension.V
        if (horizontalDartFirst != horizontalDartSecond) {
            throw LogicException()
        }

        if ((startsAndEnds(underlyingInfo)) && (startsAndEnds(second.underlyingInfo))) {
            // we're in a grid, look for common diagram elements
            val combined: MutableSet<Rectangular> = HashSet(rectangulars)
            val secondRs = second.rectangulars
            combined.retainAll(secondRs)
            if (combined.size == 1) {
                // ok, run just the single found combination
                val max = 0.0
                for (fromUI in underlyingInfo) {
                    if (combined.contains(fromUI.diagramElement)) {
                        return getMinimumDistance(horizontalDartFirst, fromUI, second, along, concave, displayer)
                    }
                }
                throw LogicException()
            }
        }

        // ok, run all the combinations
        var max = 0.0
        for (fromUI in underlyingInfo) {
            max = max(max, getMinimumDistance(horizontalDartFirst, fromUI, second, along, concave, displayer))
        }
        return max
    }
}

