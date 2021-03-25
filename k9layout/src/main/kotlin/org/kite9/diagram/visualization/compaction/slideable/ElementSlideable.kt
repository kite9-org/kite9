/**
 *
 */
package org.kite9.diagram.visualization.compaction.slideable

import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction.UnderlyingInfo
import org.kite9.diagram.visualization.display.CompleteDisplayer

/**
 * Extra functions for where a slideable represents a diagram element.
 */
open abstract class ElementSlideable(so: SlackOptimisation): Slideable(so) {

    /**
     * Works out the distance to another slideable, taking into account margins/padding etc.  If concave is set true, it means that this
     * slideable and the parallel to slideable run alongside one another.
     */
    abstract fun getMinimumDistancePossible(
        to: ElementSlideable,
        along: ElementSlideable?,
        concave: Boolean,
        displayer: CompleteDisplayer
    ): Double

    abstract fun getAdjoiningSlideables(c: Compaction): List<ElementSlideable>

    abstract fun getVerticesOnSlideable() : Set<Vertex>

    abstract fun getConnections() : Set<Connection>

    abstract fun hasUnderlying(underlying: Container): Boolean

    abstract fun getUnderlyingInfo() : Set<UnderlyingInfo>

    abstract fun getSingleSide(): Side?

    abstract fun getRectangulars(): Set<Rectangular>
}

