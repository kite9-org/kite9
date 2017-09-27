package org.kite9.diagram.batik.bridge;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.SVGGElementBridge;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.node.IdentifiableGraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Extends the regular &lt;g&gt; element bridge so that it generates {@link IdentifiableGraphicsNode}s.
 * 
 * @author robmoffat
 *
 */
public class Kite9GBridge extends SVGGElementBridge {

	public Kite9GBridge() {
		super();
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