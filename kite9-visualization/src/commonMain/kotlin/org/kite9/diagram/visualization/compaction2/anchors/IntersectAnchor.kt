package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.DiagramElement

enum class Purpose {
    PORT,
    GLYPH_LAYOUT_MIDPOINT,
    CONTAINER_LAYOUT_MIDPOINT,
    LABEL_LAYOUT;
}

/**
 * The slideable intersects the element.
 * The purpose indicates why this was created, which might be useful later.
 */
data class IntersectAnchor(override val e: DiagramElement, override val s: Purpose) : Anchor<Purpose> {

}