package org.kite9.diagram.visualization.compaction.segment

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.algorithms.so.AlignStyle
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.vertex.FanVertex
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.AlignedRectangular
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.SizedRectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.rotateClockwise
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.VerticalAlignment
import org.kite9.diagram.visualization.orthogonalization.Dart
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization

/**
 * This looks at the orthogonal representation and works out from the available Darts what
 * Vertices must be on the same Vertical or Horizontal line.
 *
 * @author robmoffat
 */
class SegmentBuilder : Logable {

    var log = Kite9Log.instance(this)

    fun buildSegmentList(o: Orthogonalization, planeDirection: Set<Direction>, direction: Dimension): List<Segment> {
        val transversePlane: MutableSet<Direction> = LinkedHashSet()
        for (d2 in planeDirection) {
            transversePlane.add(rotateClockwise(d2))
        }
        val result: MutableList<Segment> = ArrayList()
        val done: MutableSet<Vertex> = UnorderedSet()
        var segNo = 1
        for (d in o.getAllDarts()) {
            if (planeDirection.contains(d.getDrawDirection())) {
                val v = d.getFrom()
                if (!done.contains(v)) {
                    val s = Segment(
                        direction, segNo++
                    )
                    extendSegmentFromVertex(v, planeDirection, s, done)
                    s.alignStyle = getSegmentAlignStyle(s)
                    done.addAll(s.getVerticesInSegment())
                    result.add(s)
                }
            }
        }
        log.send("Segments", result)
        return result
    }

    fun getSegmentAlignStyle(s: Segment): AlignStyle? {
        val conns = s.connections
        return if (conns.size == 1) {
            val de = conns.iterator().next()
            decideConnectionSegmentAlignStyle(s, de)
        } else if (conns.size == 0) {
            val toUse : AlignedRectangular? = s.underlyingInfo
                .map { it.diagramElement }
                .filterIsInstance<AlignedRectangular>()
                .sortedWith  { a : AlignedRectangular, b: AlignedRectangular -> a.getDepth().compareTo(b.getDepth()) }
                .firstOrNull()
            if (toUse != null) {
                return decideRectangularAlignStyle(s, toUse)
            }
            null
        } else {
            throw LogicException()
        }
    }

    private fun decideRectangularAlignStyle(s: Segment, de: AlignedRectangular): AlignStyle? {
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

    private fun decideConnectionSegmentAlignStyle(s: Segment, de: Connection): AlignStyle? {
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

    private fun filterFanDirections(s: Segment, axis: (Direction) -> Boolean): Set<Direction> {
        return s.getVerticesInSegment()
            .filterIsInstance<FanVertex>()
            .flatMap { it.fanSides }
            .filter { axis (it) }
            .toSet()
    }

    private fun extendSegmentFromVertex(
        v: Vertex,
        planeDirection: Set<Direction?>,
        samePlane: Segment,
        done: MutableSet<Vertex>
    ) {
        if (done.contains(v)) return
        samePlane.addToSegment(v)
        done.add(v)
        for (e in v.getEdges()) {
            if (e is Dart && planeDirection.contains(e.getDrawDirection())) {
                val other = e.otherEnd(v)
                extendSegmentFromVertex(other, planeDirection, samePlane, done)
            }
        }
    }

    override val prefix: String
        get() = "SEGB"

    override val isLoggingEnabled: Boolean
        get() = true
}