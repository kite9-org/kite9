package org.kite9.diagram.visualization.display.style;

import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.PaintServer;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.util.CSSConstants;
import org.kite9.diagram.style.StyledDiagramElement;
import org.w3c.dom.Element;

/**
 * Encapsulates the details of a shape, whether the border of a box, a divider, a connection or an arrow end.
 * 
 * @author robmoffat
 *
 */
public class ShapeStyle extends SVGAttributedStyle {
	
	public ShapeStyle(StyledDiagramElement stylableElement) {
		super(stylableElement);
	}

	boolean invisible;
	boolean castsShadow = false;
	
	/**
	 * If true, the shape will not be drawn in the final rendered diagram
	 */
	public boolean isInvisible() {
		return invisible;
	}
	
	public Stroke getStroke() {
		Element e = getStyleElement();
		return PaintServer.convertStroke(e);
	}
		
	
	public Paint getStrokeColour() {
		return convertPaint(false);
	}

	public boolean isFilled() {
		Value v = getCSSStyleProperty(CSSConstants.CSS_FILL_PROPERTY);
		return v != null;
	}

	public Paint getBackground(Shape s) {
		return convertPaint(true);
	}
	
	public String getBackgroundKey() {
		return getCSSStyleProperty(CSSConstants.CSS_FILL_PROPERTY).getCssText();
	}
	
	public boolean castsShadow() {
		return getShadowColor() != null;
	}
	
	public int getShadowXOffset() {
		return getCSSStyleProperty("box-shadow-x-offset").getLength();
	}
	
	public int getShadowYOffset() {
		return getCSSStyleProperty("box-shadow-y-offset").getLength();
	}
	
	public Paint getShadowColor() {
		Element e = getStyleElement();
		ShapeNode sn = new ShapeNode();
		sn.setShape(new Rectangle2D.Double(1,1,1,1));
		Value v = getCSSStyleProperty("box-shadow-color");
		
		if (v == ValueConstants.NONE_VALUE) {
			return null;
		}
		
		float opacity = PaintServer.convertOpacity(v);
		return PaintServer.convertPaint(e, sn, v, opacity, getBridgeContext());
	}
	
	public Double getStrokeWidth() {
		Stroke s = getStroke();
		if (s instanceof BasicStroke) {
			return (double) ((BasicStroke)s).getLineWidth();
		} else {
			return 0d;
		}
	}
	
	public Paint convertPaint(boolean fill) {
		// css style
		Element e = getStyleElement();
		ShapeNode sn = new ShapeNode();
		sn.setShape(new Rectangle2D.Double(1,1,1,1));
		
		return fill ? PaintServer.convertFillPaint(e, sn, getBridgeContext()) : 
			PaintServer.convertStrokePaint(e, sn, getBridgeContext());			
	}

}
