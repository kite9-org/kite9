package org.kite9.diagram.visualization.display.java2d.style.shapes;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.java2d.style.DirectionalValues;
import org.kite9.diagram.visualization.display.java2d.style.FlexibleShape;

public class TubeFlexibleShape extends AbstractFlexibleShape implements FlexibleShape {

	private boolean left, right;
	
	public TubeFlexibleShape(double x, double y, boolean left, boolean right) {
		super(x, y);
		this.left = left;
		this.right = right;
		this.context = false;
	}
	
	@Override
	public DirectionalValues getBorderSizesInner(Dimension2D internalRect) {
		if (internalRect==null) {
			return DirectionalValues.ZERO;
		}
		return new DirectionalValues(0, right ? internalRect.y() / 2 : 0, 
				0, left ? internalRect.y() / 2 : 0);
	}

	@Override
	public Shape getShapeInner(double x1, double y1, double x2, double y2) {
		double r = (y2-y1)/2;
		double width = x2-x1- (left ? r:0) - (right ? r : 0);
		double x = x1 + (left ? r : 0);
		Rectangle2D main = new Rectangle2D.Double(x, y1, width, y2-y1);
		Area a1= new Area(main);
		if (left) {
			Ellipse2D eleft = new Ellipse2D.Double(x1 , y1, r*2, y2-y1);
			Area eleftArea = new Area(eleft);
			eleftArea.intersect(new Area(new Rectangle2D.Double(x-r, y1, r, y2-y1)));
			a1.add(eleftArea);
		} 
		if (right) {
			Ellipse2D eright = new Ellipse2D.Double(x2-r*2, y1, r*2, y2-y1);
			Area eRightArea = new Area(eright);
			eRightArea.intersect(new Area(new Rectangle2D.Double(x2-r, y1, r, y2-y1)));
			a1.add(eRightArea);
		}
		return a1;
	}

	@Override
	public Double getFixedAspectRatio() {
		return null;
	}


}
