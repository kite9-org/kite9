package org.kite9.diagram.visualization.display.style.shapes;

import java.awt.Shape;
import java.awt.geom.GeneralPath;

import org.kite9.diagram.visualization.display.style.DirectionalValues;

public class AbstractRectangularFlexibleShape extends AbstractReservedFlexibleShape {

	public AbstractRectangularFlexibleShape(double marginX, double marginY, DirectionalValues reserved) {
		super(marginX, marginY, reserved);
	}


	@Override
	protected Shape getShapeInner(double x1, double y1, double x2, double y2) {
		GeneralPath out = new GeneralPath();
		out.moveTo(x1+reserved.getLeft(), y1+reserved.getTop());
		drawTopShapeRight(x2-reserved.getRight(), y1 + reserved.getTop(), reserved.getTop(), out);
		drawRightShapeDown(x2 - reserved.getRight(), y2 - reserved.getBottom(), reserved.getRight(), out);
		drawBottomShapeLeft(x1 + reserved.getLeft(), y2 - reserved.getBottom(), reserved.getBottom(), out);
		drawLeftShapeUp(x1+reserved.getLeft(), y1 + reserved.getTop(), reserved.getLeft(), out);
		out.closePath();
		return out;
	}


	protected void drawTopShapeRight(double x2, double y2, double height, GeneralPath gp) {
		gp.lineTo(x2, y2);
	}
	
	protected void drawRightShapeDown(double x2, double y2, double width, GeneralPath gp) {
		gp.lineTo(x2, y2);
	}
	
	protected void drawBottomShapeLeft(double x2, double y2, double height, GeneralPath gp) {
		gp.lineTo(x2, y2);
	}
	
	protected void drawLeftShapeUp(double x2, double y2, double width, GeneralPath gp) {
		gp.lineTo(x2, y2);
	}

}
