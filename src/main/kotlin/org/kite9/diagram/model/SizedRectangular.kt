package org.kite9.diagram.model

import org.kite9.diagram.model.position.CostedDimension
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.DiagramElementSizing

/**
 * Interface for rectangular elements that can affect their own sizes.  i.e. not `Decal`s, which
 * are passive.
 */
interface SizedRectangular : Rectangular {

    /**
     * Margin is the minimum distance from this element to elements around it that it's not connected to.
     */
    fun getMargin(d: Direction): Double

    /**
     * Padding is the space inside this element that is consumed over and above the space of the child
     * elements.
     */
    fun getPadding(d: Direction): Double
    fun getSize(within: Dimension2D): CostedDimension

    /**
     * This is the user-defined minimum size the rectangle can take.  Acts as minimum limit on the
     * getSize() method.
     */
    fun getMinimumSize(): Dimension2D

    /**
     * Allows you to influence the layout:  does this element want to be big or small?
     */
    fun getSizing(horiz: Boolean): DiagramElementSizing
}