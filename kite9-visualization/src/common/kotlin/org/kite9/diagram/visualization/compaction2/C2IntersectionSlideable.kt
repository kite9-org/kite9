package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement
import kotlin.math.max


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

class C2IntersectionSlideable(
    so: C2SlackOptimisation,
    dimension: Dimension,
    val intersects: List<DiagramElement>
) : C2BufferSlideable(so, dimension) {

    override fun merge(s: C2BufferSlideable) : C2IntersectionSlideable {
        if ((s.dimension == dimension) && (s is C2IntersectionSlideable)) {
            val out = C2IntersectionSlideable(so as C2SlackOptimisation, dimension,
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

    override fun toString(): String {
        return "C2SI($number, $dimension, i/s=$intersects min=$minimumPosition, max=$maximumPosition done=$done)"
    }
}