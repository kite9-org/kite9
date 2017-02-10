package org.kite9.diagram.visualization.batik;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.GraphicsNodeBridge;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Kite9DiagramGroupBridge extends AbstractKite9GraphicsNodeBridge {
	
	public Kite9DiagramGroupBridge(GraphicsNodeLookup lookup) {
		super(lookup);
	}

	/**
	 * We are overriding this because we are going to control our 
	 * own compositing approach based on graphics layers.
	 */
	@Override
	public boolean isComposite() {
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public void processChildren(Element e, CompositeGraphicsNode into, BridgeContext ctx) {
		GVTBuilder builder = ctx.getGVTBuilder();
		for (Node n = e.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element ref = (Element)n;
                GraphicsNode refNode = builder.build(ctx, ref);
                into.getChildren().add(refNode);
            }
        }
	}
	
	/**
     * Creates the `g` for the diagram, plus layers that it contains.
     */
    public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
    	CompositeGraphicsNode out = (CompositeGraphicsNode) super.createGraphicsNode(ctx, e);
    	processChildren(e, out, ctx);
        return out;
    }
	

}
