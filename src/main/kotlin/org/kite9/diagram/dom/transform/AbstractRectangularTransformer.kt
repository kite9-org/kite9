package org.kite9.diagram.dom.transform

import org.kite9.diagram.model.Decal
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.CostedDimension2D.Companion.ZERO
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.RectangleRenderingInformation
import java.lang.StringBuilder
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.roundToLong

abstract class AbstractRectangularTransformer {

    protected fun getRectangularRenderedSize(de: DiagramElement): Dimension2D? {
        val ri = de.getRenderingInformation()
        return if (ri is RectangleRenderingInformation) {
            ri.size
        } else {
            null
        }
    }

    /**
     * Returns the position as an offset from the nearest rectangular parent container. Useful for
     * translate.
     */
    protected fun getRenderedRelativePosition(de: DiagramElement): Dimension2D {
        var position: Dimension2D = ZERO
        if (de is Decal) {
            return position
        } else if (de is Rectangular) {
            position = getOrigin(de)
        }
        val parentPosition = getParentOrigin(de)
        return position.minus(parentPosition)
    }

    fun getOrigin(de: DiagramElement): Dimension2D {
        val rri = (de as Rectangular).getRenderingInformation()
        val position = rri.position
        return position ?: ZERO
    }

    fun getParentOrigin(de: DiagramElement): Dimension2D {
        var parent = de.getParent()
        while (parent != null && parent !is Rectangular) {
            parent = parent.getParent()
        }
        if (parent is Rectangular) {
            val rri = parent.getRenderingInformation()
            val parentPosition = rri.position
            if (parentPosition != null) {
                return parentPosition
            }
        }
        return ZERO
    }

    fun oneDecimal(d: Double): String {
        val out = StringBuilder();
        var lx = (d * 10).roundToLong()

        if (lx < 0) {
            out.append("-");
            lx = abs(lx)
        }

        val decimal = lx % 10;
        val units = ((lx - decimal) / 10)

        if (units > 0) {
            out.append(units);
        }

        out.append(".")
        out.append(decimal.toString());
        return out.toString()
    }
}