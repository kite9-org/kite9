package org.kite9.diagram.common.elements;

import java.util.Map;

import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.HintMap;
import org.kite9.diagram.primitives.PositionableDiagramElement;

/**
 * A straight line from some point to some other point.
 * 
 * @author robmoffat
 *
 */
public class DebugLine implements PositionableDiagramElement {

	RouteRenderingInformation rri = null;

	String l1;
	
	public String getL1() {
		return l1;
	}

	public String getL2() {
		return l2;
	}

	String l2;
	
	public DebugLine(RouteRenderingInformation rri, String l1, String l2) {
		this.rri = rri;
		this.l1 = l1;
		this.l2 = l2;
	}
	
	@Override
	public RouteRenderingInformation getRenderingInformation() {
		return rri;
	}

	@Override
	public int compareTo(DiagramElement arg0) {
		throw new UnsupportedOperationException("Can't compare DebugLines");
	}

	@Override
	public void setRenderingInformation(RenderingInformation ri) {
		this.rri = (RouteRenderingInformation) ri;
	}

	@Override
	public HintMap getPositioningHints() {
		return null;
	}

	@Override
	public void setPositioningHints(HintMap hints) {
	}

}
