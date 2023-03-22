package org.kite9.diagram.model

import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.VerticalAlignment

interface AlignedRectangular : Rectangular {

    fun getVerticalAlignment(): VerticalAlignment
    fun getHorizontalAlignment(): HorizontalAlignment

}