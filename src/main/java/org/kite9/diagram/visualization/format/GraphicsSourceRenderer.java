package org.kite9.diagram.visualization.format;

import java.awt.Graphics2D;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.java2d.RequiresGraphics2DCompleteDisplayer;

/**
 * This is a renderer that uses an underlying {@link Graphics2D} implementation, 
 * which allows it to connect to java2d displayers.
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public interface GraphicsSourceRenderer<X> extends Renderer<X> {

	/**
	 * Returns a graphics context that the displayer can use.
	 */
	public Graphics2D getGraphicsLayer(int layer, float transparency, Dimension2D size);
	
	public void setDisplayer(RequiresGraphics2DCompleteDisplayer cd);
	
	/**
	 * Returns the size of the image for a given diagram size.
	 */
	public Dimension2D getImageSize(Dimension2D diagramSize);
}
