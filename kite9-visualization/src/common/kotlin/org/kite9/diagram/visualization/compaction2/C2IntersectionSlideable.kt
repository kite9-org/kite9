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
    val intersects: Set<DiagramElement>,
    anchors: Set<Anchor>
) : C2BufferSlideable(so, dimension, anchors) {

    constructor(so: C2SlackOptimisation, dimension: Dimension, intersects: Set<DiagramElement>) : this(so, dimension, intersects, emptySet())

    override fun merge(s: C2RectangularSlideable) : C2IntersectionSlideable {
        if (s.dimension == dimension) {
            val out = when (s) {
                is C2OrbitSlideable -> throw LogicException("Can't merge $this with $s")
                is C2IntersectionSlideable -> C2IntersectionSlideable(
                    so as C2SlackOptimisation, dimension,
                    s.intersects.plus(intersects),
                    s.anchors.plus(anchors).toSet()
                )

                else -> C2IntersectionSlideable(
                    so as C2SlackOptimisation, dimension,
                    intersects,
                    s.anchors.plus(anchors).toSet()
                )
            }

            handleMinimumMaximumAndDone(out, s)
            return out
        } else {
            throw LogicException("Can't merge $this with $s")
        }
    }

    override fun toString(): String {
        return "C2SI($number, $dimension, i/s=$intersects min=$minimumPosition, max=$maximumPosition done=$done${if (anchors.isNotEmpty()) " anchors=$anchors" else ""})"
    }
}