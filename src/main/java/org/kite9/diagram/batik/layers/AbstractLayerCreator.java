package org.kite9.diagram.batik.layers;

import java.util.ArrayList;
import java.util.List;

import org.apache.batik.anim.dom.SVG12DOMImplementation;
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
		List<GraphicsNode> nodes = initSVGGraphicsContents(theElement, ctx, de);
		for (GraphicsNode g : nodes) {
			out.add(g);
		}
		return out;
	}
	
	protected List<GraphicsNode> initSVGConnection(Element theElement, Kite9BridgeContext ctx, Connection de) {
		List<GraphicsNode> out = new ArrayList<>();
		Kite9RouteBridge bridge = new Kite9RouteBridge(de);
		GraphicsNode gn = bridge.createGraphicsNode(ctx, theElement);
		bridge.buildGraphicsNode(ctx, theElement, gn);
		out.add(gn);
		return out;
	}
	
	
	/**
	 * This implementation simply creates a group in the usual way.
	 */
	protected IdentifiableGraphicsNode createGraphicsNode(String id, Kite9BridgeContext ctx, StyledKite9SVGElement theElement, GraphicsLayerName layer, DiagramElement de) {
		GVTBuilder builder = ctx.getGVTBuilder();
		Element e = theElement.getOwnerDocument().createElementNS(SVG12DOMImplementation.SVG_NAMESPACE_URI, "g");
		IdentifiableGraphicsNode out = (IdentifiableGraphicsNode) builder.build(ctx, e);
		out.setId(id+"-"+layer.name());
		out.setLayer(layer);		
		return out;
	}
	
	/**
	 * Use this method where the DiagramElement is allowed to contain SVG contents.
	 */
	protected List<GraphicsNode> initSVGGraphicsContents(Element theElement, Kite9BridgeContext ctx, DiagramElement de) {
		if (de instanceof Connection) {
			// use the special bridge for this
			return initSVGConnection(theElement, ctx, (Connection) de); 
		}
		
		List<GraphicsNode> out = new ArrayList<>();
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
		return out;
	}
}
