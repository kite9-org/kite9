package org.kite9.diagram.batik.text;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.batik.gvt.font.GVTFontFamily;
import org.kite9.diagram.batik.bridge.LocalRenderingFlowRootElementBridge;

/**
 * Marker interface for a `Graphics2D` implementation, to say we are outputting to an SVG file.
 * 
 * Allows for handling text bounds within the graphics element.
 */
public interface ExtendedSVG {
	
	/**
	 * Returns a Java2D font which has a font-family which is resolveable.
	 * Also ensures that the correct @font-face is described in the output SVG.
	 */
	Font handleGVTFontFamilies(List<GVTFontFamily> families);
	
	/**
	 * Used by {@link LocalRenderingFlowRootElementBridge} to set the size of the 
	 * bounds used in drawing the text.
	 */
	public Rectangle2D getTextBounds();

	/**
	 * Used by {@link LocalRenderingFlowRootElementBridge} to set the size of the 
	 * bounds used in drawing the text.
	 */
	public void setTextBounds(Rectangle2D r);
	
}

