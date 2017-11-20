package org.kite9.diagram.batik.bridge;

import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.style.DiagramElementType;
import org.w3c.dom.Element;

/**
 * Handles painting for {@link DiagramElementType.SVG}
 * 
 * @author robmoffat
 *
 */
public class SVGLeafRectangularPainter extends AbstractSVGPainter<Leaf> implements RectangularPainter<Leaf> {

	private Kite9BridgeContext ctx;
	
	public SVGLeafRectangularPainter(Kite9BridgeContext ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	public Rectangle2D bounds(Element theElement) {
		GraphicsNode gn = getGraphicsNode(getDrawableContents(theElement));
		return gn.getBounds();
	}

	private GraphicsNode graphicsNodeCache;

	private GraphicsNode getGraphicsNode(Element theElement) {
		GraphicsNode out = graphicsNodeCache;
		if (out == null) {
			out = initGraphicsNode(getDrawableContents(theElement));
			graphicsNodeCache = out;
			return out;
		}
		
		return out;
	}

	
	protected GraphicsNode initGraphicsNode(Element theElement) {
		GVTBuilder builder = ctx.getGVTBuilder();
		CompositeGraphicsNode out = (CompositeGraphicsNode) builder.build(ctx, getDrawableContents(theElement));
		return out;
	}

	protected Element getDrawableContents(Element theElement) {
		return theElement;
	}


}
