package org.kite9.diagram.visualization.batik.element;

import org.kite9.diagram.adl.Decal;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.sizing.ScaledGraphics;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.visualization.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.xml.StyledKite9SVGElement;

public class ScaledSVGGraphicsImpl extends AbstractRectangularDiagramElement implements ScaledGraphics, Decal {

	public ScaledSVGGraphicsImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	/**
	 * This element is allowed to contain SVG.
	 */
	@Override
	protected IdentifiableGraphicsNode initMainGraphicsLayer() {
		IdentifiableGraphicsNode out =  super.initMainGraphicsLayer();
		initSVGGraphicsContents(out);
		return out;
	}

	@Override
	public void setParentSize(double[] x, double[] y) {
		RectangleRenderingInformation rri = getContainer().getRenderingInformation();
		getRenderingInformation().setSize(rri.getSize());
		getRenderingInformation().setPosition(rri.getPosition());
	}

}
