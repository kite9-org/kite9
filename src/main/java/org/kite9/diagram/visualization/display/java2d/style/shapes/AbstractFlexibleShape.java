package org.kite9.diagram.visualization.display.java2d.style.shapes;

import java.awt.Shape;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.java2d.style.DirectionalValues;
import org.kite9.diagram.visualization.display.java2d.style.FlexibleShape;

/**
 * Provides margins and aspect ratio functionality for the shape.
 * 
 * Margins are the area around the edge of the shape, whilst padding is inside the shape.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractFlexibleShape implements FlexibleShape {

	protected double marginX;
	protected double marginY;
	protected boolean context = true;

	@Override
	public final boolean canUseForContext() {
		return context;
	}

	public AbstractFlexibleShape(double marginX, double marginY) {
		super();
		this.marginX = marginX;
		this.marginY = marginY;
	}

	@Override
	public DirectionalValues getBorderSizes(Dimension2D internalRect) {
		DirectionalValues pad = getBorderSizesInner(internalRect);
		Double expectedAspectRatio = getFixedAspectRatio();
		if ((expectedAspectRatio != null) && (internalRect != null)) {
			double width = internalRect.getWidth() + pad.getLeft() + pad.getRight();
			double height = internalRect.getHeight() + pad.getTop() + pad.getBottom();
			double actualRatio = height / width;
			if (actualRatio > expectedAspectRatio) {
				// too high, increase padding of width
				double padWidth = ((height / expectedAspectRatio) - width) / 2;
				pad.add(new DirectionalValues(0, padWidth, 0, padWidth));
			} else if (actualRatio < expectedAspectRatio) {
				// too wide, increase padding of height
				double padHeight = ((width * expectedAspectRatio) - height) / 2;
				pad.add(new DirectionalValues(padHeight, 0, padHeight, 0));
			} 
		}
		
		pad = pad.add(new DirectionalValues(marginY, marginX, marginY, marginX));
		return pad;
	}

	protected DirectionalValues getBorderSizesInner(Dimension2D padded) {
		return DirectionalValues.ZERO;
	}
	
	@Override
	public Double getFixedAspectRatio() {
		return null;
	}

	@Override
	public DirectionalValues getMargin() {
		return new DirectionalValues(marginY, marginX, marginY, marginX);
	}

	@Override
	public Dimension2D getContentArea(Dimension2D within) {
		Double expectedAspectRatio = getFixedAspectRatio();
		within = new Dimension2D(within.x() - marginX * 2, within.y() - marginY * 2);
		within = getContentAreaInner(within);
		if (expectedAspectRatio == null) {
			return within;
		} else {
			double actualRatio = within.getHeight() / within.getWidth();
			if (actualRatio > expectedAspectRatio) {
				// too tall, make wider
				return new Dimension2D(within.getHeight() / expectedAspectRatio, within.getHeight());
			} else if (actualRatio < expectedAspectRatio) {
				// too wide, make taller
				return new Dimension2D(within.getWidth(), within.getWidth() * expectedAspectRatio);
			} else {
				return within;
			}
		}
	}

	protected Dimension2D getContentAreaInner(Dimension2D within) {
		return within;
	}

	@Override
	public Shape getShape(double x1, double y1, double x2, double y2) {
		// first remove margin
		x1 = x1 + marginX;
		x2 = x2 - marginX;
		y1 = y1 + marginY;
		y2 = y2 - marginY;
		
		Double expectedAspectRatio = getFixedAspectRatio();
		if (expectedAspectRatio==null) {
			return getShapeInner(x1, y1, x2, y2);
		} else {
			double width = x2 - x1;
			double height = y2 - y1;
			double actualRatio = height / width;
			if (actualRatio > expectedAspectRatio) {
				// too tall
				double chop = (height - (width * expectedAspectRatio)) / 2;
				y1 += chop;
				y2 -= chop;
			} else if (actualRatio < expectedAspectRatio) {
				// too wide
				double chop = (width - (height / expectedAspectRatio)) / 2;
				x1 += chop;
				x2 -= chop;
			} 
			
			return getShapeInner(x1, y1, x2, y2);
		}
	}

	/**
	 * Returns the shape, after already accounting for margins.
	 */
	protected abstract Shape getShapeInner(double x1, double y1, double x2, double y2);

	@Override
	public Shape getPerimeterShape(double x1, double y1, double x2, double y2) {
		return getShape(x1, y1, x2, y2);
	}

	@Override
	public boolean hasSpecialPerimiter() {
		return false;
	}

	
	
}