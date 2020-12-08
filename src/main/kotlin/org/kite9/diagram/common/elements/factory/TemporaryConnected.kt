package org.kite9.diagram.common.elements.factory

import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Temporary
import org.kite9.diagram.model.style.ContainerPosition

interface TemporaryConnected : Connected, Temporary {

    fun setContainerPosition(cp: ContainerPosition)
}