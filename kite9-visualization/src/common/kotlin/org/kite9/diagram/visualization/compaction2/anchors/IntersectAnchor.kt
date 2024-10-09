package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.anchors.Anchor


/**
 * The slideable intersects the element.  s isn't needed in this
 */
data class IntersectAnchor(override val e: Rectangular) : Anchor<Boolean> {

    override val s: Boolean = true;
}