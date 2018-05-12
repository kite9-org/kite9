package org.kite9.diagram.batik.painter;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.dom.painter.Painter;

public interface LeafPainter extends Painter {
	
	public Rectangle2D bounds();
	
}
