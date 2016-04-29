package org.kite9.diagram.visualization.display.style.shapes;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.style.DirectionalValues;


public class CircleFlexibleShape extends EllipseFlexibleShape {

	public CircleFlexibleShape(double marginx, double marginy) {
		super(marginx, marginy);
	}

	@Override
	public Double getFixedAspectRatio() {
		return 1.0d;
	}

	@Override
	public DirectionalValues getBorderSizesInner(Dimension2D shape) {
		if (shape==null) {
			return DirectionalValues.ZERO;
		}
		double d = Math.sqrt(shape.getWidth()*shape.getWidth() + shape.getHeight() * shape.getHeight());
		double xpad = (d - shape.getWidth()) / 2;
		double ypad = (d - shape.getHeight()) / 2;
		return new DirectionalValues(ypad, xpad, ypad, xpad);
	}

	
	

}
