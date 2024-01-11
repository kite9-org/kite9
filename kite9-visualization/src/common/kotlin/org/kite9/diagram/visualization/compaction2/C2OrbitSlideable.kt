package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.logging.LogicException


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
    val orbits: Set<RectAnchor>,
    anchors: Set<Anchor>
) : C2BufferSlideable(so, dimension, anchors) {

    constructor(so: C2SlackOptimisation, dimension: Dimension, orbits: Set<RectAnchor>) : this(so, dimension, orbits, emptySet())

    override fun merge(with: C2RectangularSlideable) : C2OrbitSlideable {
        if ((with.dimension == dimension) && (with !is C2IntersectionSlideable)) {
            val newOrbits = orbits.plus(if (with is C2OrbitSlideable) with.orbits else emptySet())
            val out = C2OrbitSlideable(so as C2SlackOptimisation, dimension,
                newOrbits,
                with.anchors.plus(anchors).toSet())

            handleMinimumMaximumAndDone(out, with)
            return out
        } else {
            throw LogicException("Can't merge $this with $with")
        }
    }

    override fun toString(): String {
        return "C2SO($number, $dimension, min=$minimumPosition, max=$maximumPosition orbits=$orbits done=$done${if (anchors.isNotEmpty()) " anchors=$anchors" else ""})"
    }
}