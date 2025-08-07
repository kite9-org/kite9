package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.model.Rectangular

enum class Purpose {
    PORT, LAYOUT, GLYPH_MIDPOINT, CONTAINER_MIDPOINT, LABEL_MIDPOINT
}

/**
 * The slideable intersects the element.
 * The purpose indicates why this was created, which might be useful later.
 */
data class IntersectAnchor(override val e: Rectangular, override val s: Purpose) : Anchor<Purpose> {

}