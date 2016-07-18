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
	
	private double pixels(String name) {
		Value v = getCSSStyleProperty(name);
		if (v instanceof FloatValue) {
			if (v.getPrimitiveType() == CSSPrimitiveValue.CSS_PX) {
				return (double) v.getFloatValue();
			} else if (v.getFloatValue() == 0) {
				return 0d;
			} else {
				throw new UnsupportedOperationException("Can only do pixels so far");
			}
		} else {
			throw new UnsupportedOperationException("Not a float value: "+v);
		}
		
	}

	private DirectionalValues getDirectionalValues(String name) {
		return new DirectionalValues(pixels(name+"-top"), pixels(name+"-right"),
				pixels(name+"-bottom"), pixels(name+"-left"));
	}

	public BoxStyle(StyledDiagramElement h) {
		super(h);
	}
	

	public DirectionalValues getInternalPadding() {
		return getDirectionalValues("padding");
	}
	
	public DirectionalValues getMargin() {
		return getDirectionalValues("margin");
	}

	public Paint getShadowPaint() {
		return new Color(.7f, .7f, .7f);
	}
}
