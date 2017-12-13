package org.kite9.diagram.model.position;

/**
 * Contains details of how to render a rectangle on screen, possibly containing some 
 * text.
 * 
 * 
 * @author robmoffat
 *
 */
public interface RectangleRenderingInformation extends RenderingInformation {
	
	public void setPosition(Dimension2D position);
	
	public void setSize(Dimension2D size);
}
