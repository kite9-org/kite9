package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular

object Util {

    fun countConnectedElements(de: DiagramElement): Int {
        var out = 0
        if (de is ConnectedRectangular) {
            out++
            if (de is Rectangular) {
                for (c in de.getContents()) {
                    out += countConnectedElements(c)
                }
            }
        }
        return out
    }

}