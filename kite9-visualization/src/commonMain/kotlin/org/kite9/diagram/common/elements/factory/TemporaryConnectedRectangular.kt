package org.kite9.diagram.common.elements.factory

import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Temporary
import org.kite9.diagram.model.style.ContainerPosition

interface TemporaryConnectedRectangular : ConnectedRectangular, Temporary {

    fun setContainerPosition(cp: ContainerPosition)
}