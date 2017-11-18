package org.kite9.diagram.batik.bridge;

import java.util.ArrayList;
import java.util.List;

import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConnectionPainter implements Painter<Connection>{

	@Override
	public Element output(Document d, Element e, Connection r) {
		// TODO Auto-generated method stub
		return null;
	}
	

	protected List<GraphicsNode> initSVGGraphicsContents() {
		List<GraphicsNode> out = new ArrayList<>();
		Kite9RouteBridge bridge = new Kite9RouteBridge(this);
		GraphicsNode gn = bridge.createGraphicsNode(ctx, theElement);
		bridge.buildGraphicsNode(ctx, theElement, gn);
		out.add(gn);
		return out;
	}
	
	

	protected void processSizesUsingTemplater(Element child, RectangleRenderingInformation rri) {
		if (rri.getSize() == null) {
			// set size of this element same as the parent
			RectangleRenderingInformation rri2 = getContainer().getRenderingInformation();
			getRenderingInformation().setSize(rri2.getSize());
			getRenderingInformation().setPosition(rri2.getPosition());
		}
		
		super.processSizesUsingTemplater(child, rri);
	}
	

}
