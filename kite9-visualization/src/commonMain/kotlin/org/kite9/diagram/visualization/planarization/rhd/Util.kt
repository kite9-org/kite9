package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement

object Util {

    fun countConnectedElements(de: DiagramElement): Int {
        var out: Int = 0
        if (de is ConnectedRectangular) {
            out++
            if (de is Container) {
                for (c: DiagramElement in (de as Container).getContents()) {
                    out += countConnectedElements(c)
                }
            }
        }
        return out
    }

}