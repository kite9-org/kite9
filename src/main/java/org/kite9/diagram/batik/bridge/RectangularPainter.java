package org.kite9.diagram.batik.bridge;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.model.Rectangular;
import org.w3c.dom.Element;

public interface RectangularPainter<X extends Rectangular> extends Painter<X> {
	
	public Rectangle2D bounds(Element in);

}
