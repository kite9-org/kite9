package org.kite9.diagram.visualization.display.style;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;

import org.kite9.diagram.visualization.display.style.io.SVGHelper;

/**
 * This applies to symbols and arrow ends:  it extends the ShapeStyle to provide details of the shape as well.
 * @author robmoffat
 *
 */
public class FixedShape extends ShapeStyle {

	private java.awt.Shape path;
	private DirectionalValues margin;
	
	public DirectionalValues getMargin() {
		return margin;
	}
	
	/**
	 * Returns a normalized path, where the shape begins at 0,0 and is sized correctly for the diagram.
	 */
	public java.awt.Shape getPath() {
		return path;
	}

	public FixedShape(SVGHelper h, Stroke stroke, Color colour, Paint background, java.awt.Shape path, DirectionalValues margin) {
		super(h, stroke, colour, background);
		this.path = path;
		this.margin = margin;
	}

}
