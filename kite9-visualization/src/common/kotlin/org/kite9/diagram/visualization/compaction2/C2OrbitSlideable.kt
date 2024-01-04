package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.DiagramElement


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
    anchors: Set<Anchor>
) : C2BufferSlideable(so, dimension, anchors) {

    constructor(so: C2SlackOptimisation, dimension: Dimension, orbits: Set<DiagramElement>) : this(so, dimension, orbits, emptySet())

    override fun merge(s: C2RectangularSlideable) : C2OrbitSlideable {
        if ((s.dimension == dimension) && (s !is C2IntersectionSlideable)) {
            val newOrbits = orbits.plus(if (s is C2OrbitSlideable) s.orbits else emptySet())
            val out = C2OrbitSlideable(so as C2SlackOptimisation, dimension,
                newOrbits,
                s.anchors.plus(anchors).toSet())

            handleMinimumMaximumAndDone(out, s)
            return out
        } else {
            throw LogicException("Can't merge $this with $s")
        }
    }

    override fun toString(): String {
        return "C2SO($number, $dimension, min=$minimumPosition, max=$maximumPosition orbits=$orbits done=$done${if (anchors.isNotEmpty()) " anchors=$anchors" else ""})"
    }
}