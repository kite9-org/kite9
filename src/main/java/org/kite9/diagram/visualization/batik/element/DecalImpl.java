package org.kite9.diagram.visualization.batik.element;

import org.kite9.diagram.adl.Decal;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.xml.StyledKite9SVGElement;

public class DecalImpl extends AbstractRectangularDiagramElement implements Decal {

	public DecalImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	@Override
	public void setParentSize(double[] x, double[] y) {
		RectangleRenderingInformation rri = getContainer().getRenderingInformation();
		getRenderingInformation().setSize(rri.getSize());
		getRenderingInformation().setPosition(rri.getPosition());
		this.parentX = x;
		this.parentY = y;
	}
	
	private double[] parentX, parentY;
	
	@Override
	protected String getReplacementValue(String prefix, String attr) {
		if ("x".equals(prefix) || "y".equals(prefix)) {
			int index = Integer.parseInt(attr);
			double v = "x".equals(prefix) ? parentX[index] : parentY[index];
			System.out.println("in "+this+" replacing "+prefix+attr);
			return ""+v;
		} else {
			return super.getReplacementValue(prefix, attr);
		}
	}

}
