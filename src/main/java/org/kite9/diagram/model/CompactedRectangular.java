package org.kite9.diagram.model;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;

/**
 * Interface for elements that are involved in compaction (i.e. will have segments)
 */
public interface CompactedRectangular extends Rectangular {

	/**
	 * Margin is the minimum distance from this element to elements around it that it's not connected to.
	 */
	public double getMargin(Direction d);
	
	/**
	 * Padding is the space inside this element that is consumed over and above the space of the child
	 * elements.
	 */
	public double getPadding(Direction d);
	
	
	public VerticalAlignment getVerticalAlignment();
	
	public HorizontalAlignment getHorizontalAlignment();

}
