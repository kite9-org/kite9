package org.kite9.diagram.visualization.planarization.rhd;

import java.util.List;
import java.util.Map;

import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.visualization.planarization.AbstractPlanarization;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader;

public abstract class RHDPlanarizationImpl extends AbstractPlanarization implements RHDPlanarization {

	public RHDPlanarizationImpl(Diagram d, Map<Container, List<Connected>> containerOrderingMap) {
		super(d);
		this.containerOrderingMap = containerOrderingMap;
	}
	
	@Override
	public RoutingInfo getPlacedPosition(DiagramElement de) {
		return rr.getPlacedPosition(de);
	}

	RoutableReader rr;

	private Map<Container, List<Connected>> containerOrderingMap;
	
	protected void setRoutableReader(RoutableReader rr) {
		this.rr = rr;
	} 

	public Map<Container, List<Connected>> getContainerOrderingMap() {
		return containerOrderingMap;
	}
}
