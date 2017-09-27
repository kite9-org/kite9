package org.kite9.diagram.batik.format;

import org.w3c.dom.Element;

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
	 * Copies some XML into the SVG file.
	 */
	public void transcribeContent(Element e);
}

