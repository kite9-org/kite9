package org.kite9.diagram.visualization.compaction.rect.second.popout

import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.isHorizontal
import org.kite9.diagram.model.style.Placement
import org.kite9.diagram.model.style.DiagramElementSizing
import org.kite9.diagram.model.style.Measurement
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction.rect.VertexTurn
import org.kite9.diagram.visualization.compaction.rect.second.prioritised.PrioritizingRectangularizer
import org.kite9.diagram.visualization.compaction.segment.SegmentSlackOptimisation
import org.kite9.diagram.visualization.compaction.segment.SegmentSlideable
import org.kite9.diagram.visualization.compaction.slideable.ElementSlideable
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.orthogonalization.DartFace
import kotlin.math.floor
import kotlin.math.max

/**
 * Does extra calculations of the [PrioritisedRectOption] to make sure that it will be
 * respecting middle-alignment of connections.
 */
abstract class AligningRectangularizer(cd: CompleteDisplayer?) : PrioritizingRectangularizer(cd) {

    /**
     * Sets up the mid-points as part of secondary sizing.
     */
    override fun performSecondarySizing(c: Compaction, stacks: Map<DartFace, MutableList<VertexTurn>>) {
        super.performSecondarySizing(c, stacks)
        stacks.values
            .flatMap { it }
            .flatMap { it.slideable.connectedRectangulars }
            .distinct()
            .forEach {
                alignConnectedRectangular(
                    it,
                    c.getVerticalSegmentSlackOptimisation().getSlideablesFor(it),
                    c,
                    c.getHorizontalSegmentSlackOptimisation(),
                    true,
                    it.getConnectionAlignment(Direction.LEFT),
                    it.getConnectionAlignment(Direction.RIGHT)
                )
                alignConnectedRectangular(
                    it,
                    c.getHorizontalSegmentSlackOptimisation().getSlideablesFor(it),
                    c,
                    c.getVerticalSegmentSlackOptimisation(),
                    false,
                    it.getConnectionAlignment(Direction.UP),
                    it.getConnectionAlignment(Direction.DOWN)
                )
            }
    }

    protected fun alignConnectedRectangular(
        cr: ConnectedRectangular,
        perp: OPair<SegmentSlideable?>,
        c: Compaction,
        sso: SegmentSlackOptimisation,
        horiz: Boolean,
        aP: Placement,
        bP: Placement
    ) {
        // collect incident ports and connections
        val portSlideablesA = getPortSlideables(perp.a!!, c)
        val portSlideablesB = getPortSlideables(perp.b!!, c)
        val connectionSlideablesA = getUnPortedConnectionSlideables(perp.a, c)
        val connectionSlideablesB = getUnPortedConnectionSlideables(perp.b, c)
        val straightA = getSlideablesForStraightConnections(connectionSlideablesA)
        val straightB = getSlideablesForStraightConnections(connectionSlideablesB)

        // fit within
        val extent = sso.getSlideablesFor(cr)
        val totalDist = extent.a!!.minimumDistanceTo(extent.b!!)

        // position ports first
        portSlideablesA.forEach { positionSlideable(it, extent, totalDist, sso, getCommonPortPlacement(perp.a, it)) }
        portSlideablesB.forEach { positionSlideable(it, extent, totalDist, sso, getCommonPortPlacement(perp.b, it)) }

        // position single connections
        when {
            connectionSlideablesA.size == 1 -> positionSlideable(
                connectionSlideablesA.first(),
                extent,
                totalDist,
                sso,
                aP
            )
            straightA.size == 1 -> positionSlideable(straightA.first(), extent, totalDist, sso, aP)
            else -> null
        }

        when {
            connectionSlideablesB.size == 1 -> positionSlideable(
                connectionSlideablesB.first(),
                extent,
                totalDist,
                sso,
                bP
            )
            straightB.size == 1 -> positionSlideable(straightB.first(), extent, totalDist, sso, bP)
            else -> null
        }

    }

    private fun getCommonPortPlacement(a: ElementSlideable, b: ElementSlideable): Placement? {
        val port = a.ports.intersect(b.ports).firstOrNull()
        return port?.getPortPosition()
    }

    fun positionSlideable(
        ps: ElementSlideable,
        extent: OPair<SegmentSlideable?>,
        totalDist: Int,
        sso: SegmentSlackOptimisation,
        p: Placement?
    ) {
        if (p == null) {
            return
        }
        val (pixelsFromStart, pixelsFromEnd) = calculatePositionForPlacement(p, totalDist)
        addWithCheck(sso, extent.a!!, pixelsFromStart.toInt(), ps, true)
        addWithCheck(sso, ps, pixelsFromEnd.toInt(), extent.b!!, true)
    }

    private fun calculatePositionForPlacement(
        p: Placement,
        totalDist: Int
    ): Pair<Double, Double> {
        val amount = p.amount

        val pixelsFromStart = when (p.type) {
            Measurement.PIXELS -> if (amount < 0) totalDist + amount else amount
            else -> totalDist * amount / 100
        }

        val pixelsFromEnd = when (p.type) {
            Measurement.PIXELS -> if (amount < 0) Math.abs(amount) else totalDist - amount
            else -> totalDist - pixelsFromStart
        }
        return Pair(pixelsFromStart, pixelsFromEnd)
    }

    private fun getSlideablesForStraightConnections(connectionSlideablesA: Set<ElementSlideable>) =
        connectionSlideablesA
            .filter {
                it.connections
                    .filter { it.getDrawDirection() != null }
                    .isNotEmpty()
            }



    protected fun addWithCheck(
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


    protected fun getPortSlideables(s: ElementSlideable, c: Compaction) : Set<ElementSlideable> {
        return s.getAdjoiningSlideables(c)
            .filter { it.ports.intersect(s.ports).isNotEmpty() }
            .toSet()
    }

    protected fun getUnPortedConnectionSlideables(s1: ElementSlideable, c: Compaction): Set<ElementSlideable> {
        var adj = s1.getAdjoiningSlideables(c)
        return adj
            .filter { it.connections.isNotEmpty() }
            .filter { it.ports.intersect(s1.ports).isEmpty() }
            .toSet()
    }


}