package org.kite9.diagram.model

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.PortPlacement

interface Port : ConnectedRectangular {

    fun getPortDirection() : Direction

    fun getPortPosition() : PortPlacement
}