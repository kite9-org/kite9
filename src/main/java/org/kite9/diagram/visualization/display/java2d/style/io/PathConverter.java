package org.kite9.diagram.visualization.display.java2d.style.io;

import java.awt.Shape;

/**
 * Provides a function for converting a Shape to a different format.
 * 
 * @author robmoffat
 *
 */
public interface PathConverter {

	public String convert(Shape shape, double xo, double yo);
}
