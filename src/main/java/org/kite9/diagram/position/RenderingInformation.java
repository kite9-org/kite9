package org.kite9.diagram.position;

import java.util.List;

import org.w3c.dom.Element;



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
	 * For storing format-specific rendering details.
	 */
	public List<Element> getDisplayData();
	
	public void setDisplayData(List<Element> o);
	
	public Dimension2D getPosition();

	public void setPosition(Dimension2D position);

	public Dimension2D getSize();

	public void setSize(Dimension2D size);
	
	public Dimension2D getInternalSize();

	public void setInternalSize(Dimension2D size);

}
