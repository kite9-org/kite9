package org.kite9.diagram.batik.format;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.kite9.diagram.batik.bridge.LocalRenderingFlowRootElementBridge;

/**
 * Marker interface for a `Graphics2D` implementation, to say we are outputting to an SVG file.
 * 
 * Allows creation of named groups and also transcribing XML content into the output.
 */
public interface ExtendedSVG {

	/**
	 * Starts a group within the svg output file.
	 */
	public void createGroup(String id);
	
	/**
	 * Finishes a group.
	 */
	public void finishGroup(String id);
	
	/**
	 * Returns a Java2D font which has a font-family which is resolveable.
	 * Also ensures that the correct @font-face is described in the output SVG.
	 */
	Font handleGVTFontFamilies(List<GVTFontFamily> families);
	
	/**
	 * Adds a font-face to the output.
	 */
	public void addFontFace(GVTFontFace face);
	
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

