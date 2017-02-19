package org.kite9.diagram.visualization.batik.bridge;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.SVGGElementBridge;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.visualization.batik.node.GraphicsNodeLookup;
import org.kite9.diagram.visualization.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.visualization.format.GraphicsLayerName;
import org.kite9.diagram.xml.XMLElement;
import org.w3c.dom.Element;

public class AbstractKite9GraphicsNodeBridge extends SVGGElementBridge {

	protected final GraphicsNodeLookup lookup;

	public AbstractKite9GraphicsNodeBridge(GraphicsNodeLookup lookup) {
		super();
		this.lookup = lookup;
	}
	
	@Override
	public String getNamespaceURI() {
		return null;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	public Bridge getInstance() {
		return new Kite9DiagramGroupBridge(lookup);
	}

	@Override
	protected void associateSVGContext(BridgeContext ctx, Element e, GraphicsNode node) {
		super.associateSVGContext(ctx, e, node);
		lookup.storeNode(getLayer(), (XMLElement) e, node);
		String id = getLayer().name()+"-"+((XMLElement)e).getID();
		((IdentifiableGraphicsNode)node).setId(id);
	}
	

	private GraphicsLayerName getLayer() {
		return GraphicsLayerName.MAIN;
	}

	@Override
	protected GraphicsNode instantiateGraphicsNode() {
		return new IdentifiableGraphicsNode();
	}
	


}