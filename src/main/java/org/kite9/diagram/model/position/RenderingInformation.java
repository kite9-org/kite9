package org.kite9.diagram.model.position;

/**
 * This holds formatting information for the graphical renderer to use.
 * 
 * @author robmoffat
 *
 */
public interface RenderingInformation {
	
	/**
	 * Returns true if this item should be drawn
	 */
	public boolean isRendered();

	public abstract void setRendered(boolean r);

	/**
	 * Returns the bounds consumed by this element
	 */
	public Dimension2D getSize();
	
	/**
	 * Returns top-left most coordinate of element.
	 */
	public Dimension2D getPosition();

}
