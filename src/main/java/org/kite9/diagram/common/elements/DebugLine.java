package org.kite9.diagram.common.elements;

import org.kite9.diagram.adl.ADLDocument;
import org.kite9.diagram.adl.LinkLineStyle;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.primitives.AbstractIdentifiableDiagramElement;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.HintMap;
import org.kite9.diagram.primitives.PositionableDiagramElement;
import org.w3c.dom.Node;

/**
 * A straight line from some point to some other point.
 * 
 * @author robmoffat
 *
 */
public class DebugLine extends AbstractIdentifiableDiagramElement implements PositionableDiagramElement {

	RouteRenderingInformation rri = null;

	public String getL1() {
		return getAttribute("l1");
	}

	public String getL2() {
		return getAttribute("l2");
	}

	
	public DebugLine(RouteRenderingInformation rri, String l1, String l2, ADLDocument doc) {
		setRenderingInformation(rri);
		setL1(l1);
		setL2(l2);
	}

	public void setL1(String l1) {
		setAttribute("l1", l1);
	}
	
	public void setL2(String l2) {
		setAttribute("l2", l2);
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
	public void setPositioningHints(HintMap hints) {
	}

	@Override
	public void setParent(Object parent) {
		// TODO Auto-generated method stub	
	}

	@Override
	protected Node newNode() {
		return new DebugLine(null, null, null, (ADLDocument) ownerDocument);
	}

	@Override
	public RenderingInformation getRenderingInformation() {
		return getBasicRenderingInformation();
	}

	@Override
	public String getShapeName() {
		return LinkLineStyle.NORMAL;
	}

}
