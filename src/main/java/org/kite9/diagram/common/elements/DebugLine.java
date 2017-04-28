package org.kite9.diagram.common.elements;

import org.kite9.diagram.batik.element.AbstractDiagramElement;
import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.position.RouteRenderingInformation;
import org.kite9.framework.xml.LinkLineStyle;

/**
 * A straight line from some point to some other point.
 * 
 * @author robmoffat
 *
 */
public class DebugLine extends AbstractDiagramElement implements DiagramElement {

	RouteRenderingInformation rri = null;
	private String l1;
	private String l2;
	
	
	public String getL1() {
		return l1;
	}

	public String getL2() {
		return l2;
	}

	
	public DebugLine(RouteRenderingInformation rri, String l1, String l2) {
		this.rri = rri;
		this.l1 = l1;
		this.l2 = l2;
	}

	public void setL1(String l1) {
		this.l1 = l1;
	}
	
	public void setL2(String l2) {
		this.l2 = l2;
	}

	@Override
	public int compareTo(DiagramElement arg0) {
		throw new UnsupportedOperationException("Can't compare DebugLines");
	}

	@Override
	public HintMap getPositioningHints() {
		return null;
	}

	@Override
	public RenderingInformation getRenderingInformation() {
		return rri;
	}

	@Override
	public String getShapeName() {
		return LinkLineStyle.NORMAL;
	}

	@Override
	public String getID() {
		return null;
	}

	@Override
	public double getMargin(Direction d) {
		return 0;
	}

}
