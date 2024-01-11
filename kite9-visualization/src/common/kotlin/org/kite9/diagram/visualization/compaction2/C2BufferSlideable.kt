package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
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


sealed class C2BufferSlideable(so: C2SlackOptimisation, dimension: Dimension, anchors: Set<Anchor>): C2RectangularSlideable(so, dimension, anchors.toMutableSet()) {

    protected fun optionalMin(s: C2BufferSlideable) = if (this.maximumPosition != null) {
        if (s.maximumPosition != null) {
            min(this.maximumPosition!!, s.maximumPosition!!)
        } else {
            this.maximumPosition
        }
    } else {
        null
    }

    abstract override fun merge(s: C2RectangularSlideable) : C2BufferSlideable

}