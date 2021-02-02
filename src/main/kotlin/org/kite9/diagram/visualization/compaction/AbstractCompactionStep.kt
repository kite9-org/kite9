package org.kite9.diagram.visualization.compaction

import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.DirectionEnforcingElement
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.visualization.compaction.segment.Segment
import org.kite9.diagram.visualization.compaction.segment.Side
import org.kite9.diagram.visualization.compaction.segment.UnderlyingInfo
import org.kite9.diagram.visualization.compaction.slideable.SegmentSlackOptimisation
import org.kite9.diagram.visualization.display.CompleteDisplayer

/**
 * This contains utility methods to deal with insertion of sub-graphs within the overall graph.
 * You should extend this wherever you need to add vertices to a segment.
 */
abstract class AbstractCompactionStep(protected val displayer: CompleteDisplayer) : CompactionStep, Logable {

    @JvmField
	protected var log = Kite9Log.instance(this)

    override val isLoggingEnabled = true

    fun getMinimumDistance(froms: Slideable<Segment>, tos: Slideable<Segment>, d: Direction): Double {
        return froms.minimumDistanceTo(tos).toDouble()
    }

    fun getMinimumDistance(first: Segment, second: Segment, along: Segment?, concave: Boolean): Double {
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
                        return getMinimumDistance(horizontalDartFirst, fromUI, second, along, concave)
                    }
                }
                throw LogicException()
            }
        }

        // ok, run all the combinations
        var max = 0.0
        for (fromUI in first.underlyingInfo) {
            max = Math.max(max, getMinimumDistance(horizontalDartFirst, fromUI, second, along, concave))
        }
        return max
    }

    private fun getMinimumDistance(
        horizontalDart: Boolean,
        fromUI: UnderlyingInfo,
        second: Segment,
        along: Segment?,
        concave: Boolean
    ): Double {
        var max = 0.0
        for (toUI in second.underlyingInfo) {
            max = Math.max(max, getMinimumDistance(horizontalDart, fromUI, toUI, along, concave))
        }
        return max
    }

    private fun getMinimumDistance(
        horizontalDart: Boolean,
        fromUI: UnderlyingInfo,
        toUI: UnderlyingInfo,
        along: Segment?,
        concave: Boolean
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

    private fun getAlongDiagramElement(along: Segment?): DiagramElement? {
        return if (along == null) {
            null
        } else along.getUnderlyingWithSide(Side.NEITHER)
            ?: return along.underlyingInfo.stream().map { (diagramElement) -> diagramElement }.findFirst()
                .orElse(null)
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

    private fun needsLength(a: DiagramElement, b: DiagramElement): Boolean {
        return if (a is DirectionEnforcingElement || b is DirectionEnforcingElement) {
            false
        } else true
    }

    protected fun separate(s1: Slideable<Segment>?, fs: FaceSide) {
        if (s1 != null) {
            for (s2 in fs.all) {
                separate(s1, s2)
            }
        }
    }

    protected fun separate(fs: FaceSide, s2: Slideable<Segment>) {
        for (s1 in fs.all) {
            separate(s1, s2)
        }
    }

    protected fun separate(s1: Slideable<Segment>?, s2: Slideable<Segment>?) {
        if ((s1!=null) && (s2 !=null)) {
            val minDistance = getMinimumDistance(s1.underlying, s2.underlying, null, true)
            s1.slackOptimisation.ensureMinimumDistance(s1, s2, minDistance.toInt())
        }
    }

    protected fun alignSingleConnections(
        c: Compaction,
        r: Connected,
        horizontal: Boolean,
        withCheck: Boolean
    ): AlignmentResult? {
        val hsso = c.getHorizontalSegmentSlackOptimisation()
        val hs = hsso.getSlideablesFor(r)
        val vsso = c.getVerticalSegmentSlackOptimisation()
        val vs = vsso.getSlideablesFor(r)
        val minimizing =
            if (r is SizedRectangular) (r as SizedRectangular).getSizing(horizontal) === DiagramElementSizing.MINIMIZE else true
        return if (horizontal) {
            alignSingleConnections(c, hs, vs, withCheck, minimizing)
        } else {
            alignSingleConnections(c, vs, hs, withCheck, minimizing)
        }
    }

    data class AlignmentResult(val midPoint: Int, val safe: Boolean)

    /**
     * Returns the half-dist value if an alignment was made, otherwise null.
     */
    protected fun alignSingleConnections(
        c: Compaction,
        perp: OPair<Slideable<Segment>?>,
        along: OPair<Slideable<Segment>?>,
        checkNeeded: Boolean,
        minimizingContainer: Boolean
    ): AlignmentResult? {
        val from = along.a!!
        val alongSSO = from.slackOptimisation as SegmentSlackOptimisation
        val to = along.b!!
        val leavingConnectionsA = getLeavingConnections(perp.a!!.underlying, c)
        val leavingConnectionsB = getLeavingConnections(perp.b!!.underlying, c)
        var halfDist = 0
        var connectionSegmentA: Slideable<Segment>? = null
        var connectionSegmentB: Slideable<Segment>? = null
        if (leavingConnectionsA.size == 1) {
            connectionSegmentA = getConnectionSegment(perp.a!!, c)
            halfDist = Math.max(halfDist, from.minimumDistanceTo(connectionSegmentA))
            halfDist = Math.max(halfDist, connectionSegmentA.minimumDistanceTo(to))
        }
        if (leavingConnectionsB.size == 1) {
            connectionSegmentB = getConnectionSegment(perp.b!!, c)
            halfDist = Math.max(halfDist, from.minimumDistanceTo(connectionSegmentB))
            halfDist = Math.max(halfDist, connectionSegmentB.minimumDistanceTo(to))
        }
        if (leavingConnectionsA.size + leavingConnectionsB.size == 0) {
            return null
        }
        val totalDist = from.minimumDistanceTo(to)
        if (totalDist > halfDist * 2) {
            val halfTotal = totalDist.toDouble() / 2.0
            halfDist = Math.floor(halfTotal).toInt()
        }
        if (halfDist > 0) {
            if (connectionSegmentA != null) {
                addWithCheck(alongSSO, from, halfDist, connectionSegmentA, checkNeeded)
                addWithCheck(alongSSO, connectionSegmentA, halfDist, to, checkNeeded)
            }
            if (connectionSegmentB != null) {
                addWithCheck(alongSSO, from, halfDist, connectionSegmentB, checkNeeded)
                addWithCheck(alongSSO, connectionSegmentB, halfDist, to, checkNeeded)
            }
            if (connectionSegmentA != null || connectionSegmentB != null) {
                val safe = leavingConnectionsA.size < 2 && leavingConnectionsB.size < 2 && minimizingContainer
                return AlignmentResult(halfDist, safe)
            }
        }
        return null
    }

    private fun addWithCheck(
        alongSSO: SegmentSlackOptimisation,
        from: Slideable<Segment>,
        dist: Int,
        to: Slideable<Segment>,
        checkNeeded: Boolean
    ) {
        if (checkNeeded) {
            if (!from.canAddMinimumForwardConstraint(to, dist)) {
                return
            }
        }
        alongSSO.ensureMinimumDistance(from, to, dist)
    }

    protected fun getLeavingConnections(
        s: Segment,
        c: Compaction
    ): Set<Connection> {
        return s.getAdjoiningSegments(c)
            .flatMap { seg: Segment -> seg.connections }
            .toSet()
    }

    private fun getConnectionSegment(s1: Slideable<Segment>, c: Compaction): Slideable<Segment> {
        return s1.underlying.getAdjoiningSegments(c)
            .filter { it.connections.isNotEmpty() }
            .map { it.slideable!! }
            .first()
    }
}