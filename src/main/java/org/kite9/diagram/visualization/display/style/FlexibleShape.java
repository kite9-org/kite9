package org.kite9.diagram.visualization.display.style;

import java.awt.Shape;

import org.kite9.diagram.position.Dimension2D;

public interface FlexibleShape {

	/**
	 * Given an internal, filled area, works out how big the border will
	 * need to be drawn for the whole shape
	 */
	public DirectionalValues getBorderSizes(Dimension2D internalRect);
	
	/**
	 * Returns the amounts of empty space around the shape that should 
	 * not be used for connections.
	 */
	public DirectionalValues getMargin(); 

	/**
	 * Given dimensions, returns the usable space within them, after removing the border sizes
	 */
	public Dimension2D getContentArea(Dimension2D within);

	public Shape getShape(double x1, double y1, double x2, double y2);
	
	public Double getFixedAspectRatio();
	
	/**
	 * Returns true if the padding on the shape is fixed irrespective of the content within it.
	 * If so, it can be used as a context.
	 */
	public boolean canUseForContext();
	
	/**
	 * Return true if this shape's perimeter is not the same as the shape we are going to draw.
	 * (Perimeter determines where links will be drawn to).
	 */
	public boolean hasSpecialPerimiter();
	
	public Shape getPerimeterShape(double x1, double y1, double x2, double y2);
	
}
