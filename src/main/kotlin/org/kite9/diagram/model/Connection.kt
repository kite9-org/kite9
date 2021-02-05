package org.kite9.diagram.model

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.RouteRenderingInformation

/**
 * A connection is a link between two Connected items within the diagram.  Connections have a notional
 * 'from' and 'to', as well as decorations to show how the links should look.
 */
interface Connection : DiagramElement, BiDirectional<Connected> {
    /**
     * The shape of the end of the edge at the from end
     */
    fun getFromDecoration(): Terminator

    /**
     * The shape of the end of the edge at the to end
     */
    fun getToDecoration(): Terminator
    fun getDecorationForEnd(end: DiagramElement): Terminator

    /**
     * The text written on the from end
     */
    fun getFromLabel(): Label?

    /**
     * Text on the to end
     */
    fun getToLabel(): Label?

    override fun getRenderingInformation(): RouteRenderingInformation

    /**
     * Returns the rank of the connection from the ordering of all the connections on the diagram.
     */
    fun getRank(): Int

    /**
     * Margin is the minimum distance from this connection to an element it is not connected with.
     */
    fun getMargin(d: Direction): Double

    /**
     * Padding is the space above or below the end of the connection such that it doesn't join
     * the corner of the element it connects to.
     */
    fun getPadding(d: Direction): Double

    /**
     * Smallest length this connection can have (when terminators are zero-size)
     */
    fun getMinimumLength(): Double

    /**
     * Arc radius used for corners and hops
     */
    fun getCornerRadius() : Double
    fun getFromArrivalSide(): Direction?
    fun getToArrivalSide(): Direction?
}