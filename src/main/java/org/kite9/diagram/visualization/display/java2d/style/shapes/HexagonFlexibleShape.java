package org.kite9.diagram.visualization.display.java2d.style.shapes;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.java2d.style.DirectionalValues;

public class HexagonFlexibleShape extends AbstractFlexibleShape {

	double tabSize;
	
	public HexagonFlexibleShape(double tabSize, double xMargin, double yMargin) {
		super(xMargin, yMargin);
		this.tabSize = tabSize;
	}
	
	@Override
	public DirectionalValues getMargin() {
		return new DirectionalValues(marginY, tabSize + marginX, marginY, tabSize+marginX);
	}

	@Override
	public Shape getShapeInner(double x1, double y1, double x2, double y2) {
		GeneralPath out = new GeneralPath();
		out.moveTo(x1, (y1+y2)/2);
		out.lineTo(x1+tabSize, y1);
		out.lineTo(x2-tabSize, y1);
		out.lineTo(x2, (y1+y2)/2);
		out.lineTo(x2-tabSize, y2);
		out.lineTo(x1+tabSize, y2);
		out.closePath();
		return out;
	}


	@Override
	protected DirectionalValues getBorderSizesInner(Dimension2D padded) {
		return new DirectionalValues(0, tabSize, 0 , tabSize);
	}

}
