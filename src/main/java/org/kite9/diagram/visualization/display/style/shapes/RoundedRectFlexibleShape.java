package org.kite9.diagram.visualization.display.style.shapes;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.DirectionalValues;

public class RoundedRectFlexibleShape extends AbstractFlexibleShape  {

	private int bevel;
	
	public RoundedRectFlexibleShape(int cornerBevel) {
		this(cornerBevel, 0,0);
	}
	
	public RoundedRectFlexibleShape(int cornerBevel, int xMargin, int yMargin) {
		super(xMargin, yMargin);
		this.bevel = cornerBevel;
	}

	@Override
	public DirectionalValues getMargin() {
		return new DirectionalValues(marginY + bevel / 2, marginX + bevel/ 2, marginY + bevel/2, marginX+bevel / 2);
	}

	@Override
	public Shape getShapeInner(double x1, double y1, double x2, double y2) {
		if (bevel == 0) {
			return new Rectangle2D.Double(x1, y1, x2-x1, y2-y1);
		} else {
			return new RoundRectangle2D.Double(x1, y1, x2-x1, y2-y1, bevel, bevel);
		}
	}

	@Override
	protected DirectionalValues getBorderSizesInner(Dimension2D padded) {
		return DirectionalValues.ZERO;
	}
	
}
