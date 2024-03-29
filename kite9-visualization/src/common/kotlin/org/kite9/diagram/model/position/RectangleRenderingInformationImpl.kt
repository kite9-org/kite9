package org.kite9.diagram.model.position

import org.kite9.diagram.common.objects.OPair
import org.kite9.diagram.common.fraction.LongFraction

class RectangleRenderingInformationImpl : AbstractRenderingInformationImpl, RectangleRenderingInformation {

    private var gx: OPair<LongFraction>? = null
    private var gy: OPair<LongFraction>? = null
    private var sx = 0
    private var sy = 0
    override var cellXPositions: DoubleArray? = doubleArrayOf()
    override var cellYPositions: DoubleArray? = doubleArrayOf()

    constructor() : super() {}
    constructor(pos: Dimension2D?, size: Dimension2D?, rendered: Boolean) {
        this.position = pos
        this.size = size
        this.rendered = rendered
    }

    override fun gridXPosition(): OPair<LongFraction>? {
        return gx
    }

    override fun gridYPosition(): OPair<LongFraction>? {
        return gy
    }

    override fun setGridXPosition(gx: OPair<LongFraction>?) {
        this.gx = gx
    }

    override fun setGridYPosition(gy: OPair<LongFraction>?) {
        this.gy = gy
    }

    override fun gridXSize(): Int {
        return sx
    }

    override fun gridYSize(): Int {
        return sy
    }

    override fun setGridXSize(x: Int) {
        sx = x
    }

    override fun setGridYSize(y: Int) {
        sy = y
    }
}