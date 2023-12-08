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

class C2OrbitSlideable(
    so: C2SlackOptimisation,
    dimension: Dimension,
    val orbits: Set<DiagramElement>,
) : C2BufferSlideable(so, dimension) {

    override fun merge(s: C2BufferSlideable) : C2OrbitSlideable {
        if ((s.dimension == dimension) && (s is C2OrbitSlideable)) {
            val out = C2OrbitSlideable(so as C2SlackOptimisation, dimension,
                s.orbits.plus(orbits).toSet())

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
        return "C2SO($number, $dimension, min=$minimumPosition, max=$maximumPosition orbits=$orbits done=$done)"
    }
}