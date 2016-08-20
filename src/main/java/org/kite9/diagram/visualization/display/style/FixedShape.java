package org.kite9.diagram.visualization.display.style;

import org.kite9.diagram.adl.LinkTerminator;
import org.kite9.diagram.adl.StyledDiagramElement;
import org.kite9.diagram.adl.Symbol;
import org.kite9.diagram.adl.Symbol.SymbolShape;
import org.kite9.diagram.visualization.display.style.io.ShapeHelper;
import org.kite9.diagram.visualization.display.style.io.StaticStyle;

/**
 * This applies to symbols and arrow ends:  it extends the ShapeStyle to provide details of the shape as well.
 * @author robmoffat
 *
 */
public class FixedShape extends ShapeStyle {
	
	public float getWidth() {
		return (float) StaticStyle.getSymbolWidth();
	}
 	
	/**
	 * Returns a normalized path, where the shape begins at 0,0 and is sized correctly for the diagram.
	 */
	public java.awt.Shape getPath() {
		if (styleElement instanceof Symbol) {
			SymbolShape shape = ((Symbol) styleElement).getShape();
			return ShapeHelper.createSymbolShape(shape, getWidth(), 0, 0);
		} else if (styleElement instanceof LinkTerminator) {
			String shape = ((LinkTerminator)styleElement).getShapeName();
			return ShapeHelper.getTerminatorShape(shape);
		} else {
			throw new UnsupportedOperationException("Can't get shape for "+styleElement);
		}
	}

	public FixedShape(StyledDiagramElement stylableElement) {
		super(stylableElement);
	}

}
