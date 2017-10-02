package org.kite9.diagram.batik.node;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.HaltingThread;
import org.kite9.diagram.batik.GraphicsLayerName;
import org.kite9.diagram.batik.format.ExtendedSVGGraphics2D;
import org.w3c.dom.Element;

public class IdentifiableGraphicsNode extends CompositeGraphicsNode implements Kite9SizedGraphicsNode {

	private String id;
	private GraphicsLayerName layer;
	private Element theElement;

	public Element getTheElement() {
		return theElement;
	}

	public void setTheElement(Element theElement) {
		this.theElement = theElement;
	}

	public GraphicsLayerName getLayer() {
		return layer;
	}

	public void setLayer(GraphicsLayerName layer) {
		this.layer = layer;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public IdentifiableGraphicsNode() {
		super();
	}
	
	

	@Override
	public void paint(Graphics2D g2d) {
		createGroup(g2d);
		super.paint(g2d);
		finishGroup(g2d);
	}

//	@Override
//	public void primitivePaint(Graphics2D g2d) {
//		if (count == 0) {
//			return;
//		}
//
//		if (g2d instanceof ExtendedSVGGraphics2D) {
//			primitivePaintOrTranscribe((ExtendedSVGGraphics2D) g2d);
//		} else {
//			super.primitivePaint(g2d);
//		}
//
//	}
//
//	private void primitivePaintOrTranscribe(ExtendedSVGGraphics2D g2d) {
//		 Element text = g2d.getDOMFactory().createElementNS(ExtendedSVGGraphics2D.SVG_NAMESPACE_URI,
//				 ExtendedSVGGraphics2D.SVG_TEXT_TAG);
//		  g2d.getDomGroupManager().addElement(text);
//		
//		// Thread.currentThread() is potentially expensive, so reuse my instance
//		// in hasBeenHalted()
//		Thread currentThread = Thread.currentThread();
//
//		// Paint children
//		for (int i = 0; i < count; ++i) {
//			if (HaltingThread.hasBeenHalted(currentThread))
//				return;
//
//			GraphicsNode node = children[i];
//			if (node == null) {
//				continue;
//			}
//			
//			if (node instanceof Kite9SizedGraphicsNode) {
//				node.paint(g2d);
//			} else {
////				g2d.transcribeXML(node);
//				node.paint(g2d);
//			}
//		}
//	}

	private void finishGroup(Graphics2D g2d) {
		if (g2d instanceof ExtendedSVGGraphics2D) {
			((ExtendedSVGGraphics2D)g2d).finishGroup(id);
		}
	}

	private void createGroup(Graphics2D g2d) {
		if (g2d instanceof ExtendedSVGGraphics2D) {
			((ExtendedSVGGraphics2D)g2d).createGroup(id);
		}
	}	
	
    /**
     * Internal Cache: Sensitive bounds.
     */
    private volatile Rectangle2D svgBounds;
	
	/**
	 * Excludes the bounds of any {@link Kite9SizedGraphicsNode} elements.
	 */
	public Rectangle2D getSVGBounds() {
		if (svgBounds != null) {
			if (svgBounds == NULL_RECT) return null;
			return svgBounds;
		}
		
		svgBounds = null;
		int i = 0;
		while (i < count) {
			GraphicsNode childNode = children[i++];
			if (!(childNode instanceof Kite9SizedGraphicsNode)) {
				Rectangle2D ctb = childNode.getTransformedBounds(IDENTITY);
				if (ctb != null) {
					if (svgBounds != null) {
						svgBounds.add(ctb);
					} else {
						svgBounds = ctb;
					}
				}
			}
		}
		
		return svgBounds;
	}

	protected void invalidateGeometryCache() {
		super.invalidateGeometryCache();
		svgBounds = null;
	}

	@Override
	public String toString() {
		return "[IdentifiableGraphicsNode id="+getId()+"]";
	}
	
	
}
