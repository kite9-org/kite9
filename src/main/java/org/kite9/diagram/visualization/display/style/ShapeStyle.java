package org.kite9.diagram.visualization.display.style;

import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.PaintServer;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.util.CSSConstants;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.framework.serialization.ADLExtensibleDOMImplementation;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * Encapsulates the details of a shape, whether the border of a box, a divider, a connection or an arrow end.
 * 
 * @author robmoffat
 *
 */
public class ShapeStyle extends SVGAttributedStyle {
	
	public ShapeStyle(DiagramElement stylableElement) {
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
	
	public float getShadowXOffset() {
		return getCSSStyleProperty("box-shadow-x-offset").getFloatValue();
	}
	
	public float getShadowYOffset() {
		return getCSSStyleProperty("box-shadow-y-offset").getFloatValue();
	}
	
	public Paint getShadowColor() {
		Element e = getStyleElement();
		ShapeNode sn = new ShapeNode();
		sn.setShape(new Rectangle2D.Double(1,1,1,1));
		
		Value v = getCSSStyleProperty("box-shadow-opacity");
		float opacity = PaintServer.convertOpacity(v);
		
		v = getCSSStyleProperty("box-shadow-color");
		
		if (v == ADLExtensibleDOMImplementation.NO_COLOR) {
			return null;
		}
		
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
	
	protected DirectionalValues getDirectionalValues(String name) {
		return new DirectionalValues(pixels(name+"-top"), pixels(name+"-right"),
				pixels(name+"-bottom"), pixels(name+"-left"));
	}
	
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
		} else if (v == null) {
			return 0;
		} else {
			throw new UnsupportedOperationException("Not a float value: "+v);
		}
		
	}
	
	public DirectionalValues getMargin() {
		return getDirectionalValues("margin");
	}

}
