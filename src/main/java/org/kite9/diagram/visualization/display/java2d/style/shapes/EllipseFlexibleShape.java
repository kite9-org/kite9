package org.kite9.diagram.visualization.display.java2d.style.shapes;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.java2d.style.DirectionalValues;

public class EllipseFlexibleShape extends AbstractFlexibleShape {
	
	public EllipseFlexibleShape(double marginx, double marginy) {
		super(marginx, marginy);
		this.context = false;
	}

	@Override
	public DirectionalValues getBorderSizesInner(Dimension2D shape) {
		if (shape==null) {
			return DirectionalValues.ZERO;
		}
		return new DirectionalValues(
				PAD_AMOUNT * shape.getHeight(),
				PAD_AMOUNT * shape.getWidth(), 
				PAD_AMOUNT * shape.getHeight(),
				PAD_AMOUNT * shape.getWidth());
	} 

	@Override
	public Shape getShapeInner(double x1, double y1, double x2, double y2) {
		double xd =0, yd =0;
		return new Ellipse2D.Double(x1-xd, y1-yd, x2-x1+xd+xd, y2-y1+yd+yd);
	}

	@Override
	public Double getFixedAspectRatio() {
		return null;
	}
	
	public static final double PAD_AMOUNT = (Math.sqrt(2) - 1) / 2;

}
