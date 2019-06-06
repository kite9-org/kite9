package org.kite9.diagram.model.position;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.common.objects.OPair;

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
	
	public int gridXSize();
	
	public int gridYSize();
	
	public OPair<BigFraction> gridXPosition();
	
	public OPair<BigFraction> gridYPosition();
	
	public void setGridXPosition(OPair<BigFraction> gx);
	
	public void setGridYPosition(OPair<BigFraction> gy);
	
	public void setGridXSize(int x);
	
	public void setGridYSize(int y);
		
}
