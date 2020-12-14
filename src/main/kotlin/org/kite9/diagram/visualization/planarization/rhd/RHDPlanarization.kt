package org.kite9.diagram.visualization.planarization.rhd

import org.kite9.diagram.common.elements.RoutingInfo
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.planarization.Planarization

/**
 * Allows the details of the positioning of diagram x/y position to be examined, as well as ordered position
 * of elements in containers with layouts.
 */
interface RHDPlanarization : Planarization {

    override fun getPlacedPosition(de: DiagramElement): RoutingInfo

    val containerOrderingMap: Map<Container, List<Connected>>
}