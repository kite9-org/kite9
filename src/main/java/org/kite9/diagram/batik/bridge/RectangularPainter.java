package org.kite9.diagram.batik.bridge;

import java.awt.geom.Rectangle2D;

import org.kite9.diagram.model.Rectangular;
import org.kite9.framework.xml.StyledKite9SVGElement;

public interface RectangularPainter<X extends Rectangular> extends Painter<X> {
	
	public Rectangle2D bounds(StyledKite9SVGElement in, X l);

}
