package org.kite9.diagram.batik.painter;

import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.DirectSVGGroupPainter;
import org.kite9.diagram.model.style.DiagramElementType;
import org.w3c.dom.Element;

/**
 * Handles painting for {@link DiagramElementType.SVG}
 * 
 * @author robmoffat
 *
 */
public class SVGLeafPainter extends DirectSVGGroupPainter implements LeafPainter {
	
	private Kite9BridgeContext ctx;
	
	public SVGLeafPainter(StyledKite9XMLElement theElement, Kite9BridgeContext ctx) {
		super(theElement, ctx.getXMLProcessor());
		this.ctx = ctx;
	}
	
	@Override
	public Rectangle2D bounds() {
		GraphicsNode gn = getGraphicsNode();
		return gn.getBounds();
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
