package org.kite9.diagram.model.position

import org.kite9.diagram.common.fraction.BigFraction
import org.kite9.diagram.common.objects.OPair

/**
 * Contains details of how to render a rectangle on screen, possibly containing some
 * text.
 *
 *
 * @author robmoffat
 */
interface RectangleRenderingInformation : RenderingInformation {

    fun gridXSize(): Int
    fun gridYSize(): Int
    fun gridXPosition(): OPair<BigFraction>?
    fun gridYPosition(): OPair<BigFraction>?
    fun setGridXPosition(gx: OPair<BigFraction>?)
    fun setGridYPosition(gy: OPair<BigFraction>?)
    fun setGridXSize(x: Int)
    fun setGridYSize(y: Int)
    var cellXPositions: DoubleArray?
    var cellYPositions: DoubleArray?
}