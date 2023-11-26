package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import kotlin.math.max
import kotlin.math.min


/**
 * This implementation of Slideable is used as a buffer between diagram
 * elements in which connections can be routed.
 *
 * Tracks the connection details within it. (TODO)
 *
 * Where <pre>intersects</pre> is set, this means the slideable cuts through the
 * elements in <pre>orbits</pre>, forming the center line.  If this is not set, the
 * slideable goes above or below the elements given in <pre>orbits</pre>.
 */

enum class BufferType { ORBITER, INTESECTER }

class C2BufferSlideable(
    so: C2SlackOptimisation,
    dimension: Dimension,
    val orbits: Set<DiagramElement>,
    val intersects: List<DiagramElement>
) : C2Slideable(so, dimension) {

    fun getBufferType() : BufferType {
        return if ((orbits.isNotEmpty()) && (intersects.isEmpty())) {
            BufferType.ORBITER
        } else if ((orbits.isEmpty()) && (intersects.isNotEmpty())) {
            BufferType.INTESECTER
        } else {
            throw LogicException("type not clear")
        }
    }

    fun merge(s: C2BufferSlideable) : C2BufferSlideable {
        if ((s.dimension == dimension) && (s.getBufferType() == getBufferType())) {
            val out = C2BufferSlideable(so as C2SlackOptimisation, dimension,
                s.orbits.plus(orbits).toSet(),
                s.intersects.plus(intersects))

            out.minimum.merge(minimum, setOf(s.minimum, minimum))
            out.minimum.merge(s.minimum, setOf(s.minimum, minimum))
            out.maximum.merge(maximum, setOf(s.maximum, maximum))
            out.maximum.merge(s.maximum, setOf(s.maximum, maximum))
            out.minimumPosition = max(this.minimumPosition, s.minimumPosition)
            out.maximumPosition = optionalMin(s)
            this.done = true
            s.done = true
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
        return "C2SB($number, $dimension, i/s=$intersects min=$minimumPosition, max=$maximumPosition orbits=$orbits done=$done)"
    }
}