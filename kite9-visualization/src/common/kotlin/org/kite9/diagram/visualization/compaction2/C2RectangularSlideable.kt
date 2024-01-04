package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Side
import kotlin.math.max
import kotlin.math.min

/**
 * This implementation of Slideable tracks underlying diagram
 * elements directly, and also allows for merging with
 * other slideables.  This is done when aligning multiple elements
 * along an axis.
 */

open class C2RectangularSlideable(
    so: C2SlackOptimisation,
    dimension: Dimension,
    val anchors: Set<Anchor>
) : C2Slideable(so, dimension) {

    open fun merge(s: C2RectangularSlideable) : C2RectangularSlideable {
        if (s.dimension == dimension) {
            val out = C2RectangularSlideable(so as C2SlackOptimisation, dimension, anchors.plus(s.anchors))
            handleMinimumMaximumAndDone(out, s)
            return out
        } else {
            throw LogicException("Can't merge $this with $s")
        }
    }

    protected fun handleMinimumMaximumAndDone(
        out: C2RectangularSlideable,
        s: C2RectangularSlideable
    ) {
        out.minimum.merge(minimum, setOf(s.minimum, minimum))
        out.minimum.merge(s.minimum, setOf(s.minimum, minimum))
        out.maximum.merge(maximum, setOf(s.maximum, maximum))
        out.maximum.merge(s.maximum, setOf(s.maximum, maximum))
        out.minimumPosition = max(this.minimumPosition, s.minimumPosition)
        out.maximumPosition = optionalMin(s)
        this.done = true
        s.done = true;
    }

    private fun optionalMin(s: C2RectangularSlideable) = if (this.maximumPosition != null) {
        if (s.maximumPosition != null) {
            min(this.maximumPosition!!, s.maximumPosition!!)
        } else {
            this.maximumPosition
        }
    } else {
        null
    }

    override fun toString(): String {
        return "C2SR($number, $dimension, anchors=$anchors, min=$minimumPosition, max=$maximumPosition done=$done)"
    }

    constructor(so: C2SlackOptimisation,
                dimension: Dimension,
                de: Rectangular,
                side: Side) : this(so, dimension, setOf(RectAnchor(de, side)))
}