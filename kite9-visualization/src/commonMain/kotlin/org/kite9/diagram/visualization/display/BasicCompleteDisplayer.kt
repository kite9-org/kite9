package org.kite9.diagram.visualization.display

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.CostedDimension2D
import org.kite9.diagram.model.position.CostedDimension2D.Companion.ZERO
import org.kite9.diagram.model.position.Dimension2D
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.RenderingInformation

class BasicCompleteDisplayer(buffer: Boolean) : AbstractCompleteDisplayer(buffer) {

    override fun size(a: DiagramElement, s: Dimension2D): CostedDimension2D {
        return if (a is SizedRectangular) {
            a.getSize(s)
        } else ZERO
    }

    override fun draw(element: DiagramElement, ri: RenderingInformation) {
        throw LogicException("Unsupported operation")
    }

    override fun getPadding(a: DiagramElement, d: Direction): Double {
        return if (a is AlignedRectangular) {
            (a as SizedRectangular).getPadding(d!!)
        } else if (a is Connection) {
            (a as SizedRectangular).getPadding(d!!)
        } else {
            0.0
        }
    }

    override fun getMargin(element: DiagramElement, d: Direction): Double {
        return if (element is AlignedRectangular) {
            (element as SizedRectangular).getMargin(d!!)
        } else if (element is Connection) {
            element.getMargin(d!!)
        } else {
            0.0
        }
    }

    override fun getLinkGutter(
        along: ConnectedRectangular,
        a: Terminator?,
        aSide: Direction?,
        b: Terminator?,
        bSide: Direction?
    ): Double {
        var length = along!!.getLinkGutter()
        val aPadding: Double = a?.getPadding(aSide!!) ?: 0.0
        val bPadding: Double = b?.getPadding(bSide!!) ?: 0.0
        val aMargin: Double = a?.getMargin(aSide!!) ?: 0.0
        val bMargin: Double = b?.getMargin(bSide!!) ?: 0.0
        val terminatorSize = aPadding + bPadding
        val margin = aMargin.coerceAtLeast(bMargin)
        length = length.coerceAtLeast(terminatorSize + margin)
        return length
    }

    public override fun getLinkMinimumLength(element: Connection, starting: Boolean, ending: Boolean): Double {
        var length = element!!.getMinimumLength()
        if (starting) {
            val term = element.getFromDecoration()
            length += term.getReservedLength()
        }
        if (ending) {
            val term = element.getToDecoration()
            length += term.getReservedLength()
        }
        return length
    }

    override fun requiresHopForVisibility(a: Connection, b: Connection): Boolean {
        return true
    }

    override fun getLinkInset(element: ConnectedRectangular, d: Direction): Double {
        return element.getLinkInset()
    }
}