package org.kite9.diagram.batik.model;

import java.util.HashMap;
import java.util.Map;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.LeafPainter;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.model.Decal;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Leaf;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DecalLeafImpl extends AbstractRectangular implements Decal, Leaf {

	public DecalLeafImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx, LeafPainter lo) {
		super(el, parent, ctx, lo);
	}

	/**
	 * Decal size is based on it's parent element, since it doesn't have a computed size of it's own.
	 */
	@Override
	protected Element paintElementToDocument(Document d) {
		RectangleRenderingInformation parentRRI = (RectangleRenderingInformation) getParent().getRenderingInformation();
		this.getRenderingInformation().setSize(parentRRI.getSize());
		this.getRenderingInformation().setPosition(parentRRI.getPosition());
		getPainter().setParameters(getXPathVariables());
		return super.paintElementToDocument(d);
	}

	@Override
	protected Map<String, String> getXPathVariables() {
		HashMap<String, String> out = new HashMap<>();
		out.put("x0", "0");
		out.put("x1", ""+ getRenderingInformation().getSize().getWidth());
		out.put("y0", "0");
		out.put("y1", ""+ getRenderingInformation().getSize().getWidth());
		return out;
		
	}
	
	
}
