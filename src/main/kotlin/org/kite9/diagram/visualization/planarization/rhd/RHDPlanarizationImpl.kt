package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.planarization.AbstractPlanarization
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader

abstract class RHDPlanarizationImpl(
    d: Diagram,
    override val containerOrderingMap: Map<Container, List<Connected>>) :
    AbstractPlanarization(
        d
    ), RHDPlanarization {

    override fun getPlacedPosition(de: DiagramElement): RoutingInfo {
        return rr!!.getPlacedPosition(de)
    }

    var rr: RoutableReader? = null
    fun setRoutableReader(rr: RoutableReader) {
        this.rr = rr
    }
}