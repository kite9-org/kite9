package org.kite9.diagram.visualization.display

import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.position.RenderingInformation

interface Displayer {

    fun draw(element: DiagramElement, ri: RenderingInformation)

}