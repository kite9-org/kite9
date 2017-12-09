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
	
	public Dimension2D getPosition();

	public void setPosition(Dimension2D position);

	public Dimension2D getSize();

	public void setSize(Dimension2D size);

}
