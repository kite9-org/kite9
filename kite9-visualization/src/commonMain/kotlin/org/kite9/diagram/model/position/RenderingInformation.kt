package org.kite9.diagram.model.position

/**
 * This holds formatting information for the graphical renderer to use.
 *
 * @author robmoffat
 */
interface RenderingInformation {
    /**
     * Returns true if this item should be drawn
     */
    var rendered: Boolean

    /**
     * Returns the bounds consumed by this element
     */
    var size: Dimension2D?

    /**
     * Returns top-left most coordinate of element.
     */
    var position: Dimension2D?
}