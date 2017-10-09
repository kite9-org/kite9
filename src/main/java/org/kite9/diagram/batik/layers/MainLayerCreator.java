package org.kite9.diagram.batik.layers;

import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.bridge.GVTBuilder;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

public class MainLayerCreator extends AbstractLayerCreator {

	/**
	 * This implementation simply creates a group in the usual way.
	 */
	public IdentifiableGraphicsNode createGraphicsNode(String id, Kite9BridgeContext ctx, StyledKite9SVGElement theElement, GraphicsLayerName layer, DiagramElement de) {
		GVTBuilder builder = ctx.getGVTBuilder();
		Element e = theElement.getOwnerDocument().createElementNS(SVG12DOMImplementation.SVG_NAMESPACE_URI, "g");
		IdentifiableGraphicsNode out = (IdentifiableGraphicsNode) builder.build(ctx, e);
		out.setId(id+"-"+layer.name());
		out.setLayer(layer);		
		return out;
	}
	
}
