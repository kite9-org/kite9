package org.kite9.diagram.batik.node;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.apache.batik.gvt.CompositeGraphicsNode;
import org.kite9.diagram.batik.format.ExtendedSVG;

public class PlaceholderGraphicsNode extends CompositeGraphicsNode {
	
	@Override
	public void paint(Graphics2D g2d) {
		if (g2d instanceof ExtendedSVG) {
			((ExtendedSVG) g2d).passthroughXML(this);
		} else {
			super.paint(g2d);
		}
	} 
	
	@Override
	public String toString() {
		return "[PlaceholderGraphicsNode]";
	}

	@Override
	public Rectangle2D getPrimitiveBounds() {
		return null;
	}

	@Override
	public Rectangle2D getGeometryBounds() {
		return null;
	}

	@Override
	public Rectangle2D getSensitiveBounds() {
		return null;
	}

	@Override
	public Shape getOutline() {
		return null;
	}
	
	
}
