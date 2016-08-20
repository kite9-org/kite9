package org.kite9.diagram.visualization.planarization.rhd;

import java.util.List;
import java.util.Map;

import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.visualization.planarization.AbstractPlanarization;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader;
import org.kite9.diagram.xml.DiagramXMLElement;

public abstract class RHDPlanarizationImpl extends AbstractPlanarization implements RHDPlanarization {

	public RHDPlanarizationImpl(DiagramXMLElement d, Map<Container, List<Contained>> containerOrderingMap) {
		super(d);
		this.containerOrderingMap = containerOrderingMap;
	}
	
	@Override
	public RoutingInfo getPlacedPosition(DiagramElement de) {
		return rr.getPlacedPosition(de);
	}

	RoutableReader rr;

	private Map<Container, List<Contained>> containerOrderingMap;
	
	protected void setRoutableReader(RoutableReader rr) {
		this.rr = rr;
	}

	public Map<Container, List<Contained>> getContainerOrderingMap() {
		return containerOrderingMap;
	}
}
