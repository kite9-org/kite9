package org.kite9.diagram.batik.bridge;

import org.apache.batik.bridge.Bridge;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GenericBridge;
import org.apache.batik.bridge.GraphicsNodeBridge;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.batik.node.PlaceholderGraphicsNode;
import org.w3c.dom.Element;

/** 
 * Bridge to ensure that even non-rendered elements get a GraphicsNode, and can therefore be output
 * by transcoding XML.
 * 
 * @author robmoffat
 *
 */
public class PlaceholderGraphicsNodeBridge implements GraphicsNodeBridge {
	
	private GenericBridge inner;
	private String ns;
	private String ln;

	public PlaceholderGraphicsNodeBridge(GenericBridge inner, String ns, String ln) {
		super();
		this.inner = inner;
		this.ns = ns;
		this.ln = ln;
	}

	@Override
	public String getNamespaceURI() {
		return ns;
	}

	@Override
	public String getLocalName() {
		return ln;
	}

	@Override
	public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
		if (inner != null) {
			inner.handleElement(ctx, e);
		}
		PlaceholderGraphicsNode out = new PlaceholderGraphicsNode();
		return out;
	}

	@Override
	public boolean isComposite() {
		return false;
	}

	@Override
	public boolean getDisplay(Element e) {
		return false;
	}

	@Override
	public Bridge getInstance() {
		return new PlaceholderGraphicsNodeBridge((GenericBridge)inner.getInstance(), ns, ln);
	}

	@Override
	public void buildGraphicsNode(BridgeContext ctx, Element e, GraphicsNode node) {
		ctx.bind(e, node);
	}
	
}
