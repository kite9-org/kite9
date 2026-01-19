package org.kite9.diagram.model

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.Placement
import org.kite9.diagram.model.style.VerticalAlignment

interface AlignedRectangular : Rectangular {

    fun getVerticalAlignment(): VerticalAlignment
    fun getHorizontalAlignment(): HorizontalAlignment

    /**
     * Returns the "middle" of the element in either axis.  This is used for alignment
     * with other elements.
     */
    fun getConnectionAlignment(d: Dimension): Placement

}