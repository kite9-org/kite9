package org.kite9.diagram.batik.text;

import java.awt.Font;
import java.util.List;

import org.apache.batik.gvt.font.GVTFontFamily;

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
	
}

