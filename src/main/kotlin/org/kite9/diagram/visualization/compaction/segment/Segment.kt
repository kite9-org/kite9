package org.kite9.diagram.visualization.compaction.segment

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.algorithms.so.AlignStyle
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.orthogonalization.Dart
import java.util.stream.Collectors

import org.kite9.diagram.visualization.compaction.Compaction
import java.util.function.Predicate


/**
 * A segment is a set of vertices that must have the same horizontal or vertical position.
 *
 *
 * @author robmoffat
 */
class Segment(val dimension: Dimension, val number: Int) : Comparable<Segment> {

    var slideable: Slideable<Segment>? = null

    val bob: Int by lazy { 6 }

    val rectangulars: Set<Rectangular> by lazy {
        underlyingInfo
            .map { it.diagramElement }
            .filterIsInstance<Rectangular>()
            .toSet()
        }

    val connections: Set<Connection> by lazy {
        underlyingInfo
            .map { it.diagramElement }
            .filter { it is Connection }
            .map { it as Connection }
            .toSet()
    }

    var alignStyle : AlignStyle? = AlignStyle.CENTER // default

    val underlyingInfo: Set<UnderlyingInfo> by lazy {
        dartsInSegment
            .flatMap { convertUnderlyingToUnderlyingInfo(it) }
            .filter { a: UnderlyingInfo -> a.diagramElement != null }
            .toSet()
    }


    var singleSide: Side? = null
        get() {
            if (field == null) {
                field = underlyingInfo!!.stream()
                    .map { ui: UnderlyingInfo -> ui.side }
                    .reduce(null) { a: Side?, b: Side? -> sideReduce(a, b) }
            }
            return field
        }
        private set

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

    private fun convertUnderlyingToUnderlyingInfo(d: Dart): Iterable<UnderlyingInfo> {
        val diagramElements: Map<DiagramElement, Direction> = d.getDiagramElements()
        return diagramElements.keys.map { toUnderlyingInfo(
            it,
            diagramElements[it])
        }
    }

    private fun toUnderlyingInfo(de: DiagramElement, d: Direction?): UnderlyingInfo {
        return UnderlyingInfo(
            de,
            getSideFromDirection(de, d)
        )
    }

    private fun getSideFromDirection(de: DiagramElement, d: Direction?): Side {
        return if (de is BiDirectional<*>) {
            Side.NEITHER
        } else if (de is Rectangular) {
            when (d) {
                Direction.DOWN, Direction.RIGHT -> Side.END
                Direction.UP, Direction.LEFT -> Side.START
                else -> Side.NEITHER
            }
        } else {
            throw LogicException()
        }
    }

    fun addToSegment(v: Vertex) {
        verticesInSegment.add(v)
    }

    val identifier: String
        get() = "$dimension ($number $underlyingInfo $alignStyle )"

    override fun toString(): String {
        return "$identifier $verticesInSegment"
    }

    private val verticesInSegment: MutableSet<Vertex> = LinkedHashSet()

    fun getVerticesInSegment(): Set<Vertex> {
        return verticesInSegment
    }

    fun connects(a: Vertex, b: Vertex): Boolean {
        return inSegment(a) && inSegment(b)
    }

    private fun inSegment(b: Vertex): Boolean {
        return verticesInSegment.contains(b)
    }

    /**
     * De-facto ordering for segments.
     */
    override fun compareTo(o: Segment): Int {
        return slideable!!.minimumPosition.compareTo(o.slideable!!.minimumPosition)
    }

    /**
     * This is a utility method, used to set the positions of the darts for the diagram
     */
    val dartsInSegment: Collection<Dart> by lazy {
        verticesInSegment
            .flatMap { it.getEdges() }
            .filterIsInstance<Dart>()
            .filter {
                if (dimension === Dimension.H) {
                    (it.getDrawDirection() === Direction.LEFT || it.getDrawDirection() === Direction.RIGHT)
                } else {
                    (it.getDrawDirection() === Direction.UP || it.getDrawDirection() === Direction.DOWN)
                }
            }
    }

    /**
     * Used for deciding whether a segment should be pushed left/right/up/down according to it's connections.
     */
    val adjoiningSegmentBalance: Int
        get() {
            val isHorizontal = dimension === Dimension.H
            return getVerticesInSegment().stream()
                .mapToInt { v: Vertex ->
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


    private var leavingSegments: Set<Segment>? = null

    fun getAdjoiningSegments(c: Compaction): Set<Segment?>? {
        if (leavingSegments == null) {
            val isHorizontal = dimension === Dimension.H

            // find segments that meet this one
            leavingSegments = getVerticesInSegment()
                .map {
                    if (isHorizontal) c.verticalVertexSegmentMap[it] else c.horizontalVertexSegmentMap[it]
                }
                .filterNotNull()
                .toSet()
        }

        return leavingSegments
    }

}