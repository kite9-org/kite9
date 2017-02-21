package org.kite9.diagram.visualization.batik.node;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.kite9.diagram.visualization.batik.format.GroupManagingSVGGraphics2D;

public class IdentifiableGraphicsNode extends CompositeGraphicsNode {

	private String id;

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
		System.out.println("painting " +id);
		((GroupManagingSVGGraphics2D)g2d).createGroup(id);
		super.paint(g2d);
		((GroupManagingSVGGraphics2D)g2d).finishGroup(id);
		System.out.println("finished painting " +id);
	}
	

	@Override
	public void primitivePaint(Graphics2D g2d) {
		System.out.println("starting primitive paint"+id);
		super.primitivePaint(g2d);
		System.out.println("ending primitive paint"+id);
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
