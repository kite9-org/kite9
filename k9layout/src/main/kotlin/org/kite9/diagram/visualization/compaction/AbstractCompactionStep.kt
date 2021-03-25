package org.kite9.diagram.visualization.compaction

import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.visualization.display.CompleteDisplayer
import kotlin.math.floor
import kotlin.math.max

/**
 * This contains utility methods to deal with insertion of sub-graphs within the overall graph.
 * You should extend this wherever you need to add vertices to a segment.
 *
 *
 */
abstract class AbstractCompactionStep(protected val displayer: CompleteDisplayer) : CompactionStep, Logable {

	protected var log = Kite9Log.instance(this)

    override val isLoggingEnabled = true

    fun getMinimumDistance(froms: ElementSlideable, tos: ElementSlideable, d: Direction): Double {
        return froms.minimumDistanceTo(tos).toDouble()
    }

    fun getMinimumDistance(first: ElementSlideable, second: ElementSlideable, along: ElementSlideable?, concave: Boolean): Double {
        return first.getMinimumDistancePossible(second, along, concave, displayer)
    }

    protected fun separate(s1: ElementSlideable?, fs: FaceSide) {
        if (s1 != null) {
            for (s2 in fs.all) {
                separate(s1, s2)
            }
        }
    }

    protected fun separate(fs: FaceSide, s2: ElementSlideable) {
        for (s1 in fs.all) {
            separate(s1, s2)
        }
    }

    protected fun separate(s1: ElementSlideable?, s2: ElementSlideable?) {
        if ((s1!=null) && (s2 !=null)) {
            val minDistance = getMinimumDistance(s1, s2, null, true)
            s1.so.ensureMinimumDistance(s1, s2, minDistance.toInt())
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
        perp: OPair<out ElementSlideable?>,
        along: OPair<out ElementSlideable?>,
        checkNeeded: Boolean,
        minimizingContainer: Boolean
    ): AlignmentResult? {
        val from = along.a!!
        val alongSSO = from.so
        val to = along.b!!
        val leavingConnectionsA = getLeavingConnections(perp.a, c)
        val leavingConnectionsB = getLeavingConnections(perp.b, c)
        var halfDist = 0
        var connectionSegmentA: ElementSlideable? = null
        var connectionSegmentB: ElementSlideable? = null
        if (leavingConnectionsA.size == 1) {
            connectionSegmentA = getConnectionSegment(perp.a!!, c)
            halfDist = max(halfDist, from.minimumDistanceTo(connectionSegmentA))
            halfDist = max(halfDist, connectionSegmentA.minimumDistanceTo(to))
        }
        if (leavingConnectionsB.size == 1) {
            connectionSegmentB = getConnectionSegment(perp.b!!, c)
            halfDist = max(halfDist, from.minimumDistanceTo(connectionSegmentB))
            halfDist = max(halfDist, connectionSegmentB.minimumDistanceTo(to))
        }
        if (leavingConnectionsA.size + leavingConnectionsB.size == 0) {
            return null
        }
        val totalDist = from.minimumDistanceTo(to)
        if (totalDist > halfDist * 2) {
            val halfTotal = totalDist.toDouble() / 2.0
            halfDist = floor(halfTotal).toInt()
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
        alongSSO: SlackOptimisation,
        from: ElementSlideable,
        dist: Int,
        to: ElementSlideable,
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
        s: ElementSlideable?,
        c: Compaction
    ): Set<Connection> {
        if (s==null) {
            return emptySet()
        }

        return s.getAdjoiningSlideables(c)
            .flatMap { it.getConnections() }
            .toSet()
    }

    private fun getConnectionSegment(s1: ElementSlideable, c: Compaction): ElementSlideable {
        return s1.getAdjoiningSlideables(c)
            .filter { it.getConnections().isNotEmpty() }
            .first()
    }
}