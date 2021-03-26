/**
 *
 */
package org.kite9.diagram.visualization.compaction.slideable

import org.kite9.diagram.common.algorithms.so.AlignStyle
import org.kite9.diagram.common.algorithms.so.SlackOptimisation
import org.kite9.diagram.common.algorithms.so.Slideable
import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.vertex.Vertex
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction.UnderlyingInfo
import org.kite9.diagram.visualization.display.CompleteDisplayer

/**
 * Extra functions for where a slideable represents a diagram element.
 */
open abstract class ElementSlideable(
    so: SlackOptimisation,
    val dimension: Dimension,
    val number: Int): Slideable(so) {

    /**
     * Works out the distance to another slideable, taking into account margins/padding etc.  If concave is set true, it means that this
     * slideable and the parallel to slideable run alongside one another.
     */
    abstract fun getMinimumDistance(
        to: ElementSlideable,
        along: ElementSlideable?,
        concave: Boolean,
        displayer: CompleteDisplayer
    ): Double

    abstract fun getAdjoiningSlideables(c: Compaction): Set<ElementSlideable>

    abstract val verticesOnSlideable: Set<Vertex>

    abstract val connections: Set<Connection>

    abstract fun hasUnderlying(underlying: DiagramElement): Boolean

    abstract val underlyingInfo: Set<UnderlyingInfo>

    abstract val singleSide: Side?

    abstract val rectangulars: Set<Rectangular>

    abstract fun getUnderlyingWithSide(s: Side): DiagramElement?

    abstract fun hasUnderlying(des: Set<DiagramElement>): Boolean

    abstract val alignStyle: AlignStyle?

    abstract val adjoiningSegmentBalance: Int
}

