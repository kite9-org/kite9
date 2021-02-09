package org.kite9.diagram.batik.painter;

import java.awt.geom.AffineTransform;

import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.painter.DirectSVGGroupPainter;
import org.kite9.diagram.dom.painter.LeafPainter;
import org.kite9.diagram.model.position.Rectangle2D;
import org.kite9.diagram.model.style.DiagramElementType;
import org.w3c.dom.Element;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.common.Kite9XMLProcessingException;

/**
 * Handles painting for {@link DiagramElementType.SVG}
 * 
 * @author robmoffat
 *
 */
public class SVGLeafPainter extends DirectSVGGroupPainter implements LeafPainter {
	
	private Kite9BridgeContext ctx;
	protected AffineTransform transform;

	public SVGLeafPainter(StyledKite9XMLElement theElement, Kite9BridgeContext ctx) {
		super(theElement);
		this.ctx = ctx;
	}
	
	@Override
	public Rectangle2D bounds() {
		GraphicsNode gn = getGraphicsNode();
		if (transform != null) {
			java.awt.geom.Rectangle2D g = gn.getTransformedBounds(transform);
			if (g==null) {
				return null;
			}
			return new Rectangle2D(g.getX(), g.getY(), g.getWidth(), g.getHeight());
		} else {
			java.awt.geom.Rectangle2D g = gn.getBounds();
			if (g==null) {
				return null;
			}
			return new Rectangle2D(g.getX(), g.getY(), g.getWidth(), g.getHeight());
		}
		
	}

	private GraphicsNode graphicsNodeCache;

	protected GraphicsNode getGraphicsNode() {
		GraphicsNode out = graphicsNodeCache;
		if (out == null) {
			out = initGraphicsNode(theElement, ctx);
			graphicsNodeCache = out;
			return out;
		}
		
		return out;
	}
	
	protected GraphicsNode initGraphicsNode(Element e, Kite9BridgeContext ctx) {
		ensureNoChildKite9Elements(e);
		GVTBuilder builder = ctx.getGVTBuilder();
		CompositeGraphicsNode out = (CompositeGraphicsNode) builder.build(ctx, e);
		return out;
	}

	protected void ensureNoChildKite9Elements(Element e) {
		if (e instanceof Kite9XMLElement) {
			if (((Kite9XMLElement) e).iterator().hasNext()) {
				throw new Kite9XMLProcessingException(e+" shouldn't have nested Kite9 elements - it's supposed to be a leaf (svg elements only). ", e);
			}
		} else {
			throw new Kite9XMLProcessingException("How is "+e+" not a Kite9 element? ", e);
		}
	}

}
