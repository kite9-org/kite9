package org.kite9.diagram.model

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.style.Placement

interface Port : Connected{

    fun getPortDirection(): Direction

    fun getPortPosition(): Placement

}