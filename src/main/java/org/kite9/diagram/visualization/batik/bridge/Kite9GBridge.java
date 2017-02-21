package org.kite9.diagram.visualization.batik.bridge;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGGElementBridge;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.visualization.batik.node.IdentifiableGraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Extends the regular <g> element bridge so that it generates {@link IdentifiableGraphicsNode}s.
 * 
 * @author robmoffat
 *
 */
public class Kite9GBridge extends SVGGElementBridge {

	public Kite9GBridge() {
		super();
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
	            if (refNode != null) {
	            	into.getChildren().add(refNode);
	            }
	        }
	    }
	}

	@Override
	protected IdentifiableGraphicsNode instantiateGraphicsNode() {
		return new IdentifiableGraphicsNode();
	}

}