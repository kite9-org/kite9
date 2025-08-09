package org.kite9.diagram.visualization.compaction2.anchors

import org.kite9.diagram.model.Rectangular

enum class Purpose {
    PORT,
    CONTAINER_NON_LAYOUT,
    GLYPH_LAYOUT_MIDPOINT,
    CONTAINER_LAYOUT_MIDPOINT,
    LABEL_LAYOUT;

    fun isLayout() : Boolean {
        return when(this) {
            PORT,
            CONTAINER_NON_LAYOUT -> false
            GLYPH_LAYOUT_MIDPOINT,
            CONTAINER_LAYOUT_MIDPOINT,
            LABEL_LAYOUT -> true
        }
    }
}

/**
 * The slideable intersects the element.
 * The purpose indicates why this was created, which might be useful later.
 */
data class IntersectAnchor(override val e: Rectangular, override val s: Purpose) : Anchor<Purpose> {

}