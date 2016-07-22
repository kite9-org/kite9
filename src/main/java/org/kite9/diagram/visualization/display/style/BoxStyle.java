package org.kite9.diagram.visualization.display.style;

import java.awt.Color;
import java.awt.Paint;

import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.style.StyledDiagramElement;
import org.w3c.dom.css.CSSPrimitiveValue;


/**
 * Handles basic formatting of a box, which is compatible with CSS box model.
 * 
 * @author robmoffat
 */
public class BoxStyle extends ShapeStyle {


	public BoxStyle(StyledDiagramElement h) {
		super(h);
	}
	

	public DirectionalValues getInternalPadding() {
		return getDirectionalValues("padding");
	}

	public Paint getShadowPaint() {
		return new Color(.7f, .7f, .7f);
	}
}
