package org.kite9.diagram.model.position

import org.kite9.diagram.common.fraction.LongFraction
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
    fun gridXPosition(): OPair<LongFraction>?
    fun gridYPosition(): OPair<LongFraction>?
    fun setGridXPosition(gx: OPair<LongFraction>?)
    fun setGridYPosition(gy: OPair<LongFraction>?)
    fun setGridXSize(x: Int)
    fun setGridYSize(y: Int)
    var cellXPositions: DoubleArray?
    var cellYPositions: DoubleArray?
}