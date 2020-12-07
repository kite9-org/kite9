package org.kite9.diagram.visualization.planarization.rhd;

import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.visualization.planarization.Planarization;

/**
 * Allows the details of the positioning of diagram x/y position to be examined, as well as ordered position
 * of elements in containers with layouts.
 */
public interface RHDPlanarization extends Planarization {

	public RoutingInfo getPlacedPosition(DiagramElement de);

	public Map<Container, List<Connected>> getContainerOrderingMap();

}