package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.compaction.Side
import kotlin.math.max
import kotlin.math.min

enum class Purpose { ROUTE, EDGE }

data class Anchor(val e: DiagramElement, val s: Side)

/**
 * This implementation of Slideable tracks underlying diagram
 * elements directly, and also allows for merging with
 * other slideables.  This is done when aligning multiple elements
 * along an axis.
 */
class C2Slideable(
    so: C2SlackOptimisation,
    val dimension: Dimension,
    val purpose: Purpose,
    val anchors: Set<Anchor>
) : Slideable(so) {

    val number: Int = nextNumber()

    fun merge(s: C2Slideable) : C2Slideable {
        if ((s.purpose == purpose) && (s.dimension == dimension)) {
            val newAnchors = listOf(anchors, s.anchors).flatten().toSet()
            val out = C2Slideable(so as C2SlackOptimisation, dimension, purpose, newAnchors)
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

    private fun optionalMin(s: C2Slideable) = if (this.maximumPosition != null) {
        if (s.maximumPosition != null) {
            min(this.maximumPosition!!, s.maximumPosition!!)
        } else {
            this.maximumPosition
        }
    } else {
        null
    }

    override fun toString(): String {
        return "C2S($number, $dimension, $purpose, anchors=$anchors, min=$minimumPosition, max=$maximumPosition)"
    }


    companion object {

        var n: Int = 0

        fun nextNumber() : Int {
            n++
            return n;
        }

    }

    constructor(so: C2SlackOptimisation,
                dimension: Dimension,
                purpose: Purpose,
                de: DiagramElement,
                side: Side) : this(so, dimension, purpose, setOf(Anchor(de, side)))
}