package org.kite9.diagram.model;

import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;

/**
 * Interface for rectangular elements that can affect their own sizes.  i.e. not `Decal`s, which
 * are passive.
 */
public interface SizedRectangular extends Rectangular {

	/**
	 * Margin is the minimum distance from this element to elements around it that it's not connected to.
	 */
	public double getMargin(Direction d);
	
	/**
	 * Padding is the space inside this element that is consumed over and above the space of the child
	 * elements.
	 */
	public double getPadding(Direction d);
	
	
	public CostedDimension getSize(Dimension2D within);
	
	/**
	 * This is the user-defined minimum size the rectangle can take.  Acts as minimum limit on the
	 * getSize() method.
	 */
	public Dimension2D getMinimumSize();

}
