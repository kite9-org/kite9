package org.kite9.diagram.dom.model

import org.kite9.diagram.model.style.HorizontalAlignment
import org.kite9.diagram.model.style.VerticalAlignment

/**
 * This interface is implemented by elements whose alignment
 * can be determined during the layout process.  Typically, this
 * refers to labels.
 *
 */
interface LayoutAligns {

    fun setVerticalAlignment(va: VerticalAlignment)
    fun setHorizontalAlignment(ha: HorizontalAlignment)

}