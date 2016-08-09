package org.kite9.diagram.visualization.display.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.PaintServer;
import org.apache.batik.css.engine.value.InheritValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg12.SVG12ValueConstants;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVG12CSSConstants;
import org.kite9.diagram.adl.StyledDiagramElement;
import org.kite9.diagram.visualization.display.components.AbstractADLDisplayer.Justification;
import org.kite9.diagram.visualization.display.style.io.FontHelper;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSS2Properties;

/**
 * Captures the details of the format that text will appear in.
 * 
 * @author robmoffat
 *
 */
public class TextStyle extends SVGAttributedStyle {
	
	public TextStyle(StyledDiagramElement h) {
		super(h);
	}
	
	public Color getColor() {
		Element e = getStyleElement();
		ShapeNode sn = new ShapeNode();
		sn.setShape(new Rectangle2D.Double(1,1,1,1));
		
		return (Color)  PaintServer.convertStrokePaint(e, sn, getBridgeContext());
	}

	public Font getFont() {
		Value fontName = getCSSStyleProperty(CSSConstants.CSS_FONT_FAMILY_PROPERTY);
		Value fontSize = getCSSStyleProperty(CSSConstants.CSS_FONT_SIZE_PROPERTY);
		
		return FontHelper.getFont(stripQuotes(fontName.getCssText()), (int) fontSize.getFloatValue());
	}

	private String stripQuotes(String cssText) {
		return cssText.substring(1, cssText.length()-1);
	}

	public Justification getJust() {
		Value textAlign = getCSSStyleProperty( SVG12CSSConstants.CSS_TEXT_ALIGN_PROPERTY);
		if (textAlign instanceof InheritValue) {
			return Justification.CENTER;
		} else {
			if (textAlign.getStringValue()==SVG12CSSConstants.CSS_START_VALUE) {
				return Justification.LEFT;
			} else if (textAlign.getStringValue()==SVG12CSSConstants.CSS_END_VALUE) {
				return Justification.RIGHT;
			} else {
				return Justification.CENTER;
			}
		}
	}
	
}
