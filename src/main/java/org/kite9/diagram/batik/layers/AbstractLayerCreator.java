package org.kite9.diagram.batik.layers;

import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.Kite9RouteBridge;
import org.kite9.diagram.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.Kite9XMLElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractLayerCreator implements LayerCreator {
	
	@Override
	public IdentifiableGraphicsNode createLayer(String id, Kite9BridgeContext ctx, StyledKite9SVGElement theElement, GraphicsLayerName layer, DiagramElement de) {
		IdentifiableGraphicsNode out = createGraphicsNode(id, ctx, theElement, layer, de);
		initSVGGraphicsContents(out, theElement, ctx, de);
		return out;
	}
	
	protected void initSVGConnection(IdentifiableGraphicsNode out, Element theElement, Kite9BridgeContext ctx, Connection de) {
		Kite9RouteBridge bridge = new Kite9RouteBridge(de);
		GraphicsNode gn = bridge.createGraphicsNode(ctx, theElement);
		bridge.buildGraphicsNode(ctx, theElement, gn);
		out.add(gn);
	}
	
	
	public abstract IdentifiableGraphicsNode createGraphicsNode(String id, Kite9BridgeContext ctx, StyledKite9SVGElement theElement, GraphicsLayerName layer, DiagramElement de);
	
	/**
	 * Use this method where the DiagramElement is allowed to contain SVG contents.
	 */
	protected void initSVGGraphicsContents(IdentifiableGraphicsNode out, Element theElement, Kite9BridgeContext ctx, DiagramElement de) {
		
		if (de instanceof Connection) {
			// use the special bridge for this
			initSVGConnection(out, theElement, ctx, (Connection) de); 
			return;
		}
		
		
		GVTBuilder builder = ctx.getGVTBuilder();
		NodeList childNodes = theElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			if ((child instanceof Element) && (!(child instanceof Kite9XMLElement))) {
				// get access to the bridge, to create a graphics node.
				GraphicsNode node = builder.build(ctx, (Element) child);
				if (node != null) {
					out.add(node);
				}
			}
		}
	}
}
