package org.kite9.diagram.visualization.batik.bridge;

import java.awt.Graphics2D;

import org.apache.batik.anim.dom.AbstractElement;
import org.apache.batik.anim.dom.SVGOMAnimatedPathData;
import org.apache.batik.anim.dom.SVGOMPathElement;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.CSSUtilities;
import org.apache.batik.bridge.SVGPathElementBridge;
import org.apache.batik.dom.svg.SVGAnimatedPathDataSupport;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.parser.AWTPathProducer;
import org.kite9.diagram.visualization.batik.node.Kite9SizedGraphicsNode;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGPathSegList;

/**
 * This adds functionality to SVG to produce paths which contain references to the size
 * of the parent container.
 * 
 * @author robmoffat
 *
 */
public class ScalablePathElementBridge extends SVGPathElementBridge {

	
	
	@Override
	protected void buildShape(BridgeContext ctx, Element e, ShapeNode shapeNode) {
		SVGOMAnimatedPathData _d = new SVGOMAnimatedPathData((AbstractElement) e, "some", "nonsense", "M0 0 L1 1z");
		createShapeFromPath(e, shapeNode, _d);
		((DeferredShapeNode)shapeNode).setElement(e);
	}


	private static void createShapeFromPath(Element e, ShapeNode shapeNode, SVGOMAnimatedPathData _d) {
		AWTPathProducer app = new AWTPathProducer();
		_d.check();
		SVGPathSegList p = _d.getAnimatedPathSegList();
		app.setWindingRule(CSSUtilities.convertFillRule(e));
		SVGAnimatedPathDataSupport.handlePathSegList(p, app);
		shapeNode.setShape(app.getShape());
	
	}

	public static class DeferredShapeNode extends ShapeNode implements Kite9SizedGraphicsNode {

		private Element fromElement;
		private boolean shapeSet = false;
		
		@Override
		public void paint(Graphics2D g2d) {
			if (!shapeSet) {
				SVGOMPathElement pe = (SVGOMPathElement) fromElement;
				SVGOMAnimatedPathData _d = pe.getAnimatedPathData();
				createShapeFromPath(pe, this, _d);
				shapeSet = true;
			} 
			
			super.paint(g2d);
		}

		public void setElement(Element e) {
			this.fromElement = e;
		}
		
	}
	
	
	@Override
	protected GraphicsNode instantiateGraphicsNode() {
		return new DeferredShapeNode();
	}
	
	

}
