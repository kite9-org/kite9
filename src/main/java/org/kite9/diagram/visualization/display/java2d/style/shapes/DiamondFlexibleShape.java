package org.kite9.diagram.visualization.display.java2d.style.shapes;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.java2d.style.DirectionalValues;
import org.kite9.diagram.visualization.display.java2d.style.FlexibleShape;

public class DiamondFlexibleShape extends AbstractFlexibleShape implements FlexibleShape {

	public DiamondFlexibleShape(double x, double y) {
		super(x, y);
		this.context = false;
	}
	
	@Override
	public DirectionalValues getBorderSizesInner(Dimension2D internalRect) {
		if (internalRect==null) {
			return DirectionalValues.ZERO;
		}
		return new DirectionalValues(internalRect.y() / 2, internalRect.x() / 2, 
				internalRect.y() / 2, internalRect.x() / 2);
	}

	@Override
	public Shape getShapeInner(double x1, double y1, double x2, double y2) {
		double xd = 0;
		double yd = 0;
		GeneralPath out = new GeneralPath();
		out.moveTo(x1-xd, (y1+y2) / 2);
		out.lineTo((x1+x2)/2, y1-yd);
		out.lineTo(x2+xd, (y1+y2)/2);
		out.lineTo((x1+x2)/2, y2+yd);
		out.closePath();
		return out;
	}

	@Override
	public Double getFixedAspectRatio() {
		return null;
	}

	

}
