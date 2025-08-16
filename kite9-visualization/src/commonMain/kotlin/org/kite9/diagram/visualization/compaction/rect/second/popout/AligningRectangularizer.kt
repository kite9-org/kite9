package org.kite9.diagram.visualization.compaction.rect.second.popout

import org.kite9.diagram.model.*
import org.kite9.diagram.model.style.Measurement
import org.kite9.diagram.model.style.Placement
import kotlin.math.abs

/**
 * Does extra calculations of the [PrioritisedRectOption] to make sure that it will be
 * respecting middle-alignment of connections.
 */
abstract class AligningRectangularizer {

    companion object {

        fun calculatePositionForPlacement(
            p: Placement,
            totalDist: Int
        ): Pair<Double, Double> {
            val amount = p.amount

            val pixelsFromStart = when (p.type) {
                Measurement.PIXELS -> if (amount < 0) totalDist + amount else amount
                else -> totalDist * amount / 100
            }

            val pixelsFromEnd = when (p.type) {
                Measurement.PIXELS -> if (amount < 0) abs(amount) else totalDist - amount
                else -> totalDist - pixelsFromStart
            }
            return Pair(pixelsFromStart, pixelsFromEnd)
        }
    }

}