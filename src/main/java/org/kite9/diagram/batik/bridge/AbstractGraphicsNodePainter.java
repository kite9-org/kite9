package org.kite9.diagram.batik.bridge;

import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.model.DiagramElement;
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

}
