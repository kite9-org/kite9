package org.kite9.diagram.batik.bridge;

import static org.apache.batik.util.SVGConstants.SVG_G_TAG;
import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;

import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.format.ExtendedSVGGeneratorContext;
import org.kite9.diagram.batik.format.ExtendedSVGGraphics2D;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides functionality to the painter to construct a graphics node, and paint using the graphics node.
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public abstract class AbstractGraphicsNodePainter<X extends DiagramElement> extends AbstractDirectSVGPainter<X> {

	private Kite9BridgeContext ctx;
	
	public AbstractGraphicsNodePainter(Kite9BridgeContext ctx) {
		super();
		this.ctx = ctx;
	}

	private GraphicsNode graphicsNodeCache;

	protected GraphicsNode getGraphicsNode(Element theElement) {
		GraphicsNode out = graphicsNodeCache;
		if (out == null) {
			out = initGraphicsNode(theElement);
			graphicsNodeCache = out;
			return out;
		}
		
		return out;
	}
	
	protected GraphicsNode initGraphicsNode(Element theElement) {
		GVTBuilder builder = ctx.getGVTBuilder();
		CompositeGraphicsNode out = (CompositeGraphicsNode) builder.build(ctx, theElement);
		return out;
	}
	
	public Element outputViaGraphicsNode(Document d, StyledKite9SVGElement theElement, @SuppressWarnings("unused") X r) {
		GraphicsNode node = getGraphicsNode(theElement);
		Element groupElem = d.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		ExtendedSVGGeneratorContext genCtx = ExtendedSVGGeneratorContext.buildSVGGeneratorContext(d, null, null);
		ExtendedSVGGraphics2D g2d = new ExtendedSVGGraphics2D(genCtx, groupElem);
		g2d.transform(node.getInverseTransform());
		node.paint(g2d);
		groupElem = g2d.getTopLevelGroup(true);
		
		return groupElem;
	}

}
