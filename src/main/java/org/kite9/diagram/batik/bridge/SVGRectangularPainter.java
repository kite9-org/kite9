package org.kite9.diagram.batik.bridge;

import java.awt.geom.Rectangle2D;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.templater.Kite9ExpandingCopier;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles painting for {@link DiagramElementType.SVG}
 * 
 * @author robmoffat
 *
 */
public class SVGRectangularPainter implements RectangularPainter<Leaf> {

	private Kite9BridgeContext ctx;
	
	public SVGRectangularPainter(Kite9BridgeContext ctx) {
		super();
		this.ctx = ctx;
	}

	/**
	 * The basic output approach is to turn any DiagramElement into a <g> tag, with the same ID set
	 * as the DiagramElement.  
	 */
	@Override
	public Element output(Document d, StyledKite9SVGElement theElement, Leaf r) {
		Element out = d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12OMDocument.SVG_G_TAG);
		new Kite9ExpandingCopier("", out).processContents(theElement);
		out.setAttribute(SVG12OMDocument.SVG_ID_ATTRIBUTE, r.getID());
		out.setAttribute("class", theElement.getCSSClass());
		out.setAttribute("style", theElement.getAttribute("style"));
		return out;
	}

	@Override
	public Rectangle2D bounds(Element theElement) {
		GraphicsNode gn = getGraphicsNode(theElement);
		return gn.getBounds();
	}

	private GraphicsNode graphicsNodeCache;

	private GraphicsNode getGraphicsNode(Element theElement) {
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
