package org.kite9.diagram.position;

/**
 * Contains details of how to render a rectangle on screen, possibly containing some 
 * text.
 * 
 * 
 * @author robmoffat
 *
 */
public interface RectangleRenderingInformation extends RenderingInformation {

	public boolean isMultipleHorizontalLinks();

	public void setMultipleHorizontalLinks(boolean multipleHorizontalLinks);

	public boolean isMultipleVerticalLinks();

	public void setMultipleVerticalLinks(boolean multipleVerticalLinks);
	
	public HPos getHorizontalJustification();

	public void setHorizontalJustification(HPos horizontalJustification);

	public VPos getVerticalJustification();

	public void setVerticalJustification(VPos verticalJustification);

	
}
