package org.kite9.diagram.visualization.display.style.shapes;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.visualization.display.style.DirectionalValues;

public abstract class AbstractTopIconFlexibleShape extends AbstractRectangularFlexibleShape {

	private double iconWidth;
	
	public AbstractTopIconFlexibleShape(double marginX, double marginY, DirectionalValues reserved, double iconWidth) {
		super(marginX, marginY, reserved);
		this.iconWidth = iconWidth;
	}

	@Override
	public Shape getPerimeterShape(double x1, double y1, double x2, double y2) {
		double sx = x1+reserved.getLeft();
		double sy = y1+reserved.getTop();
		Shape s1 = new Rectangle2D.Double(
				sx, 
				sy, 
				x2-reserved.getRight() - sx,
				y2-reserved.getBottom() - sy);
		double iconWidth = getTopIconWidth();
		double iconHeight = reserved.getTop();
		double midWidth = (x1+x2) / 2f;
		
		Rectangle2D r = new Rectangle2D.Double(midWidth - iconWidth / 2, y1, iconWidth, iconHeight);
		Area a1 = new Area(s1);
		a1.add(new Area(r));
		return a1;
	}
	
	@Override
	protected void drawTopShapeRight(double x2, double y2,
			double height, GeneralPath gp) {
		Point2D p = gp.getCurrentPoint();
		double width = getTopIconWidth();
		double middle = (p.getX() + x2) / 2.0f;
		
		drawTopIcon(middle -width/2, p.getY() - height, middle+width/2, y2, gp);	
		gp.moveTo(p.getX(), p.getY());
		super.drawTopShapeRight(x2, y2, height, gp);
	}
	
	protected abstract void drawTopIcon(double x1, double y1, double x2, double y2, GeneralPath gp);

	public double getTopIconWidth() {
		return iconWidth;
	}

	@Override
	public boolean hasSpecialPerimiter() {
		return true;
	}
	
	

}
