package org.kite9.diagram.batik.bridge;

import java.awt.Graphics2D;
import java.awt.Shape;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.SVGShapeElementBridge;
import org.apache.batik.gvt.ShapeNode;
import org.kite9.diagram.model.Connection;
import org.w3c.dom.Element;

/**
 * This is a special bridge used on the connections. 
 * 
 * @author robmoffat
 *
 */
public class Kite9RouteBridge extends SVGShapeElementBridge {

	private Connection c;
	
	public Kite9RouteBridge(Connection c) {
		super();
		this.c = c;
	}

	@Override
	public String getLocalName() {
		return null;
	}

	@Override
	protected void buildShape(BridgeContext ctx, Element e, ShapeNode node) {
		RoutePainter rp = new RoutePainter();
		Shape s = rp.drawRouting(c.getRenderingInformation(), rp.NULL_END_DISPLAYER, rp.NULL_END_DISPLAYER, rp.LINK_HOP_DISPLAYER, false);
		node.setShape(s);
	}

}
