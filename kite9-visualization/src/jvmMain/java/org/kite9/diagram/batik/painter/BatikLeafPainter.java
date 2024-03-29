package org.kite9.diagram.batik.painter;

import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.painter.DirectSVGGroupPainter;
import org.kite9.diagram.dom.painter.LeafPainter;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Rectangle2D;
import org.w3c.dom.Element;

import java.awt.geom.AffineTransform;

public class BatikLeafPainter extends DirectSVGGroupPainter implements LeafPainter {
	
	protected ElementContext ctx;

	public BatikLeafPainter(Element theElement, ElementContext ctx) {
		super(theElement);
		this.ctx = ctx;
	}
	
	@Override
	public Rectangle2D bounds() {
		return ctx.bounds(getTheElement());
	}

	private GraphicsNode graphicsNodeCache;

	protected GraphicsNode getGraphicsNode() {
		GraphicsNode out = graphicsNodeCache;
		if (out == null) {
			out = initGraphicsNode(getTheElement(), (Kite9BridgeContext) ctx);
			graphicsNodeCache = out;
			return out;
		}
		
		return out;
	}
	
	protected GraphicsNode initGraphicsNode(Element e, Kite9BridgeContext ctx) {
		DiagramElement de = ctx.getRegisteredDiagramElement(e);
		ensureNoChildKite9Elements(de, e);
		GVTBuilder builder = ctx.getGVTBuilder();
		GraphicsNode out = builder.build(ctx, e);
		return out;
	}

	protected void ensureNoChildKite9Elements(DiagramElement de, Element e) {
		if (ctx.getChildDiagramElements(de).size() > 0) {
			throw new Kite9XMLProcessingException(de+" shouldn't have nested Kite9 elements - it's supposed to be a leaf (svg elements only). ", e);
		}
	}

}
