package org.kite9.diagram.visualization.display.style;

import org.apache.batik.css.engine.CSSStylableElement;
import org.kite9.diagram.style.StyledDiagramElement;

/**
 * This applies to symbols and arrow ends:  it extends the ShapeStyle to provide details of the shape as well.
 * @author robmoffat
 *
 */
public class FixedShape extends ShapeStyle {

	public DirectionalValues getMargin() {
		throw new RuntimeException("Not implemented yet");
	}
	
	/**
	 * Returns a normalized path, where the shape begins at 0,0 and is sized correctly for the diagram.
	 */
	public java.awt.Shape getPath() {
		throw new RuntimeException("Not implemented yet");
	}

	public FixedShape(StyledDiagramElement stylableElement) {
		super(stylableElement);
	}

}
