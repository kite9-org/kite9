package org.kite9.diagram.model.style;

import java.awt.Paint;

import org.apache.batik.css.engine.value.Value;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.EnumValue;
import org.kite9.framework.xml.StyledKite9SVGElement;

public class BoxShadow {

	private final double x;
	private final double y;
	private final BoxShadowType type;
	private final double blur;
	private final double radius;
	private final Paint paint;

	public BoxShadow(double x, double y, BoxShadowType type, double blur, double radius, Paint paint) {
		super();
		this.x = x;
		this.y = y;
		this.type = type;
		this.blur = blur;
		this.radius = radius;
		this.paint = paint;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public BoxShadowType getType() {
		return type;
	}

	public double getBlur() {
		return blur;
	}

	public double getRadius() {
		return radius;
	}

	public Paint getPaint() {
		return paint;
	}
	
	public static BoxShadow constructBoxShadow(StyledKite9SVGElement styledKite9SVGElement) {
		EnumValue ev = (EnumValue) styledKite9SVGElement.getCSSStyleProperty(CSSConstants.BOX_SHADOW_TYPE_PROPERTY);
		BoxShadowType bst = (BoxShadowType) ev.getTheValue();
		switch (bst) {
		case NONE:
			return null;
		case INSET:
			return new BoxShadow(0, 0, bst, getBlur(styledKite9SVGElement), getSpread(styledKite9SVGElement), getPaint(styledKite9SVGElement));
		case OUTER:
			float x = styledKite9SVGElement.getCSSStyleProperty(CSSConstants.BOX_SHADOW_X_OFFSET_PROPERTY).getFloatValue();
			float y = styledKite9SVGElement.getCSSStyleProperty(CSSConstants.BOX_SHADOW_X_OFFSET_PROPERTY).getFloatValue();
			return new BoxShadow(x, y, bst, getBlur(styledKite9SVGElement), getSpread(styledKite9SVGElement), getPaint(styledKite9SVGElement));

		}

		return null;
	}

	private static Paint getPaint(StyledKite9SVGElement e) {
		Value v = e.getCSSStyleProperty(CSSConstants.BOX_SHADOW_COLOR_PROPERTY);
		
		if (v == null) {
			v =  e.getCSSStyleProperty(org.apache.batik.util.CSSConstants.CSS_COLOR_PROPERTY);
		}
		
	}

	private static float getSpread(StyledKite9SVGElement e) {
		return e.getCSSStyleProperty(CSSConstants.BOX_SHADOW_SPREAD_PROPERTY).getFloatValue();
	}

	private static float getBlur(StyledKite9SVGElement e) {
		return e.getCSSStyleProperty(CSSConstants.BOX_SHADOW_BLUR_PROPERTY).getFloatValue();
	}
}
