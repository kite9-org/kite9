package org.kite9.diagram.visualization.display.style.io;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.PaintServer;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.AbstractValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.SVGPaintManager;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.util.CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * Handles conversion of raphael gradient paints to and from css.
 * @author robmoffat
 * @TODO: remove this.
 */
public class GradientPaintValueManager extends SVGPaintManager {

	public static final GradientValue NO_GRADIENT = new GradientValue(null);

	public static class GradientValue extends AbstractValue {

		String v;

		@Override
		public String getCssText() {
			return v;
		}

		public GradientValue(String v) {
			this.v = v;
		}
	}

	SVGHelper helper;

	public GradientPaintValueManager(SVGHelper helper) {
		super(CSSConstants.CSS_FILL_PROPERTY, NO_GRADIENT);
		this.helper = helper;
	}

	@Override
	public boolean isInheritedProperty() {
		return true;
	}
//
//	@Override
//	public boolean isAnimatableProperty() {
//		return false;
//	}
//
//	@Override
//	public boolean isAdditiveProperty() {
//		return false;
//	}
//
//	@Override
//	public int getPropertyType() {
//		return SVGTypes.TYPE_UNKNOWN;
//	}
//
//	@Override
//	public Value getDefaultValue() {
//		return NO_GRADIENT;
//	}

	@Override
	public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
		if (lu.getLexicalUnitType() == LexicalUnit.SAC_STRING_VALUE) {
			return new GradientValue(lu.getStringValue());
		} else {
			return super.createValue(lu, engine);
		}
	}

	@Override
	public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
		if (value instanceof GradientValue) {
			return value;
		} else {
			return super.computeValue(elt, pseudo, engine, idx, sm, value);
		}
	}

	@Override
	public String getPropertyName() {
		return CSSConstants.CSS_FILL_PROPERTY;
	}

	public Paint convert(Value v) {

		String color = v.getCssText();
		if (color == null) {
			return null;
		}
		// raphael format gradient
		String[] parts = color.split("-");
		Color[] colors = new Color[parts.length - 1];
		Point2D start, end;
		float[] dists = new float[parts.length - 1];

		for (int i = 1; i < parts.length; i++) {
			String p = parts[i];
			Color c;
			float dist;
			if (p.contains(":")) {
				String[] split = p.split(":");
				c = parseColour(split[0]);
				dist = Float.parseFloat(split[1]);
			} else {
				c = parseColour(p);
				dist = i == 1 ? 0 : 1;
			}
			dists[i - 1] = dist;
			colors[i - 1] = c;
		}

		String ang = parts[0];
		if (ang.equals("270")) {
			start = new Point2D.Double(.5, 0);
			end = new Point2D.Double(.5, 1);
		} else if (ang.equals("0")) {
			start = new Point2D.Double(0, .5);
			end = new Point2D.Double(1, .5);
		} else if (ang.equals("90")) {
			start = new Point2D.Double(.5, 1);
			end = new Point2D.Double(.5, 0);
		} else {
			start = new Point2D.Double(1, .5);
			end = new Point2D.Double(0, .5);
		}

		return new LinearGradientPaint(start, end, dists, colors);

	}

	private Color parseColour(String c) {
		SVGOMDocument doc = helper.getDoc();
		Element e = helper.createStyleableElement();
		e.setAttribute("style", "stroke: " + c);
		ShapeNode sn = new ShapeNode();
		sn.setShape(new Rectangle2D.Double(1, 1, 1, 1));
		return (Color) PaintServer.convertStrokePaint(e, sn, helper.getBridge());
	}

	public static String convert(LinearGradientPaint lgp) {
		double angle = ((Math.atan2(lgp.getEndPoint().getX() - lgp.getStartPoint().getX(), lgp.getEndPoint().getY()
				- lgp.getStartPoint().getY()) * 360) + 270) % 360;

		float[] dists = lgp.getFractions();
		Color[] cols = lgp.getColors();
		StringBuilder out = new StringBuilder();
		out.append(Math.round(angle) + "-");
		for (int i = 0; i < cols.length; i++) {
			out.append(convertColour(cols[i]));
			if (i < cols.length - 1) {
				if (i > 0) {
					out.append(":" + Math.round(dists[i - 1] * 100));
				}
				out.append("-");
			}
		}

		return out.toString();
	}

	public static String convertColour(Color c) {
		if (c == null) {
			return "#000";
		} else {
			return "#" + String.format("%02x", c.getRed()) + String.format("%02x", c.getGreen())
					+ String.format("%02x", c.getBlue());
		}
	}

}
