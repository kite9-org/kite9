package org.kite9.diagram.batik.element;

import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.LeafPainter;
import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DecalLeafImpl extends AbstractRectangularDiagramElement implements Decal, Leaf {

	public DecalLeafImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, LeafPainter lo) {
		super(el, parent, ctx, lo);
	}

	@Override
	public Rectangle2D getBounds() {
		throw new Kite9ProcessingException("Decal doesn't have bounds");
	}
	
	/**
	 * Decal size is based on it's parent element, since it doesn't have a computed size of it's own.
	 */
	@Override
	protected Element paintElementToDocument(Document d) {
		RectangleRenderingInformation parentRRI = (RectangleRenderingInformation) getParent().getRenderingInformation();
		this.getRenderingInformation().setSize(parentRRI.getSize());
		this.getRenderingInformation().setPosition(parentRRI.getPosition());
		return super.paintElementToDocument(d);
	}

	@Override
	protected Map<String, String> getReplacementMap(StyledKite9SVGElement theElement) {
		Map<String, String> out = super.getReplacementMap(theElement);
		Dimension2D size = getRenderingInformation().getSize();
		double width = size.getWidth();
		double height = size.getHeight();
		out.put("x0", "0");
		out.put("y0", "0");
		out.put("x1", ""+width);
		out.put("y1", ""+height);	
		return out;
	}
}
