package org.kite9.diagram.visualization.display.java2d.style.shapes;

import java.awt.Shape;
import java.awt.geom.Line2D;

public class DividerFlexibleShape extends AbstractFlexibleShape {

	public DividerFlexibleShape() {
		super(0, 0);
		this.context = false;
	}

	@Override
	protected Shape getShapeInner(double x1, double y1, double x2, double y2) {
		return new Line2D.Double(x1, (y1+y2)/2, x2, (y1+y2)/2);
	}

}
