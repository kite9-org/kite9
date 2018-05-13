package org.kite9.diagram.batik.painter;

import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.painter.DirectSVGGroupPainter;
import org.w3c.dom.Element;

/**
 * Provides functionality to the painter to construct a graphics node, and paint using the graphics node.
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public abstract class AbstractGraphicsNodePainter extends DirectSVGGroupPainter {

	protected Kite9BridgeContext ctx;
	
	public AbstractGraphicsNodePainter(StyledKite9SVGElement theElement, Kite9BridgeContext ctx) {
		super(theElement, ctx.getXMLProcessor());
		this.ctx = ctx;
	}

	private GraphicsNode graphicsNodeCache;

	protected GraphicsNode getGraphicsNode() {
		GraphicsNode out = graphicsNodeCache;
		if (out == null) {
			out = initGraphicsNode(getContents(), ctx);
			graphicsNodeCache = out;
			return out;
		}
		
		return out;
	}
	
	public static GraphicsNode initGraphicsNode(Element e, Kite9BridgeContext ctx) {
		GVTBuilder builder = ctx.getGVTBuilder();
		CompositeGraphicsNode out = (CompositeGraphicsNode) builder.build(ctx, e);
		return out;
	}

}
