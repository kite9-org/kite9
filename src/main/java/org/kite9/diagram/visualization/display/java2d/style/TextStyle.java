package org.kite9.diagram.visualization.display.java2d.style;

import static org.apache.batik.util.SVGConstants.SVG_FILL_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.PaintServer;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.svggen.SVGPaint;
import org.apache.batik.svggen.SVGPaintDescriptor;
import org.apache.batik.util.CSSConstants;
import org.kite9.diagram.visualization.display.java2d.adl_basic.AbstractADLDisplayer.Justification;
import org.kite9.diagram.visualization.display.java2d.style.io.SVGHelper;
import org.kite9.diagram.visualization.display.java2d.style.sheets.AbstractStylesheet;
import org.w3c.dom.Element;

/**
 * Captures the details of the format that text will appear in.
 * 
 * @author robmoffat
 *
 */
public class TextStyle extends SVGAttributedStyle {
	
	Justification just;

	public TextStyle(SVGHelper h, Font font, Color color, Justification just, String fontName) {
		super(h);
		attr.put("font-family", fontName);
		attr.put("font-size", ""+font.getSize());
		
		if (color != null) {
			final SVGPaint paintConverter = new SVGPaint(getHelper().getContext());
			SVGPaintDescriptor pd = paintConverter.toSVG(color);
			outputAttributes(pd, SVG_FILL_ATTRIBUTE, SVG_FILL_OPACITY_ATTRIBUTE);
			if (pd.getDef() != null) {
				getHelper().getRoot().appendChild(pd.getDef());
			}
		}
		
		this.just = just;
	}
	
	public Color getColor() {
		Element e = getStyleElement();
		ShapeNode sn = new ShapeNode();
		sn.setShape(new Rectangle2D.Double(1,1,1,1));
		
		return (Color)  PaintServer.convertFillPaint(e, sn, getHelper().getBridge());
	}

	public Font getFont() {
		Value fontName = SVGHelper.getCSSStyleProperty(getStyleElement(), CSSConstants.CSS_FONT_FAMILY_PROPERTY);
		Value fontSize = SVGHelper.getCSSStyleProperty(getStyleElement(), CSSConstants.CSS_FONT_SIZE_PROPERTY);
//		return AbstractStylesheet.getFont(fontName.getCssText(), fontSize.);
		return AbstractStylesheet.getFont(stripQuotes(fontName.getCssText()), (int) fontSize.getFloatValue());
	}

	private String stripQuotes(String cssText) {
		return cssText.substring(1, cssText.length()-1);
	}

	public Justification getJust() {
		return just;
	}
	
}
