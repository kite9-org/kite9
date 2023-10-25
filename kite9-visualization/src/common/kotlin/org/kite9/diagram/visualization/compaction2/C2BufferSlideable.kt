package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.compaction.Side
import kotlin.math.max
import kotlin.math.min


/**
 * This implementation of Slideable is used as a buffer between diagram
 * elements in which connections can be routed.
 *
 * Tracks the connection details within it.
 */

class C2BufferSlideable(
    so: C2SlackOptimisation,
    dimension: Dimension
) : C2Slideable(so, dimension) {

    fun merge(s: C2BufferSlideable) : C2BufferSlideable {
        if (s.dimension == dimension) {
            val out = C2BufferSlideable(so as C2SlackOptimisation, dimension)
            out.minimum.merge(minimum)
            out.minimum.merge(s.minimum)
            out.maximum.merge(maximum)
            out.maximum.merge(s.maximum)
            out.minimumPosition = max(this.minimumPosition, s.minimumPosition)
            out.maximumPosition = optionalMin(s)
            return out
        } else {
            throw LogicException("Can't merge $this with $s")
        }
    }

    private fun optionalMin(s: C2BufferSlideable) = if (this.maximumPosition != null) {
        if (s.maximumPosition != null) {
            min(this.maximumPosition!!, s.maximumPosition!!)
        } else {
            this.maximumPosition
        }
    } else {
        null
    }

    override fun toString(): String {
        return "C2SB($number, $dimension, min=$minimumPosition, max=$maximumPosition)"
    }
}