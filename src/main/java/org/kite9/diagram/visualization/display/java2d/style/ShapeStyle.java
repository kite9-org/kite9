package org.kite9.diagram.visualization.display.java2d.style;

import static org.apache.batik.util.SVGConstants.SVG_FILL_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_DASHOFFSET_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_LINECAP_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_LINEJOIN_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_MITERLIMIT_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.apache.batik.bridge.PaintServer;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.gvt.ShapeNode;
import org.apache.batik.svggen.SVGBasicStroke;
import org.apache.batik.svggen.SVGPaint;
import org.apache.batik.svggen.SVGPaintDescriptor;
import org.apache.batik.svggen.SVGStrokeDescriptor;
import org.apache.batik.util.CSSConstants;
import org.kite9.diagram.visualization.display.java2d.style.io.GradientPaintValueManager;
import org.kite9.diagram.visualization.display.java2d.style.io.GradientPaintValueManager.GradientValue;
import org.kite9.diagram.visualization.display.java2d.style.io.SVGHelper;
import org.w3c.dom.Element;

/**
 * Encapsulates the details of a shape, whether the border of a box, a divider, a connection or an arrow end.
 * 
 * @author robmoffat
 *
 */
public class ShapeStyle extends SVGAttributedStyle {

	public static String[] STROKE_ATTRIBUTES = new String[] { SVG_STROKE_MITERLIMIT_ATTRIBUTE, 
			SVG_STROKE_LINECAP_ATTRIBUTE, 
			SVG_STROKE_DASHARRAY_ATTRIBUTE, 
			SVG_STROKE_LINEJOIN_ATTRIBUTE, 
			SVG_STROKE_DASHOFFSET_ATTRIBUTE,
			SVG_STROKE_WIDTH_ATTRIBUTE };
	
	
	boolean castsShadow;
	boolean invisible;
	Stroke strokeOverride;
	Stroke cached;
	
	/**
	 * If true, the shape will not be drawn in the final rendered diagram
	 */
	public boolean isInvisible() {
		return invisible;
	}
	
	public Stroke getStroke() {
		if (strokeOverride!=null) {
			return strokeOverride;
		}
		Element e = getStyleElement();
		return PaintServer.convertStroke(e);
	}
		
	
	public Paint getStrokeColour() {
		return convertPaint(false);
	}

	public boolean isFilled() {
		Value v = SVGHelper.getCSSStyleProperty(getStyleElement(), CSSConstants.CSS_FILL_PROPERTY);
		return v != GradientPaintValueManager.NO_GRADIENT;
	}

	public Paint getBackground(Shape s) {
		return convertPaint(true);
	}
	
	public boolean castsShadow() {
		return castsShadow;
	}

	public Double getStrokeWidth() {
		Stroke s = getStroke();
		if (s instanceof BasicStroke) {
			return (double) ((BasicStroke)s).getLineWidth();
		} else {
			return 0d;
		}
	}

	public ShapeStyle(SVGHelper h, Stroke stroke, Color colour, Paint background) {
		this(h, stroke, colour, background, null, false, true);
	}

	
	
	public ShapeStyle(SVGHelper h, Stroke stroke, Color colour, Paint background, String dashArray, boolean invisible, boolean shadow) {
		super(h);
		this.invisible = invisible;
		this.castsShadow = shadow;
		
		
		if (background != null) {
			convertPaint(background, true);
		} else {
			set(SVG_FILL_ATTRIBUTE, "none");
		}
		
		if (colour != null) {
			convertPaint(colour, false);
		} else {
			set(SVG_STROKE_ATTRIBUTE, "none");
		}
		
		if (stroke instanceof BasicStroke) {
			final SVGBasicStroke strokeConverter = new SVGBasicStroke(getHelper().getContext());
			SVGStrokeDescriptor sd = strokeConverter.toSVG((BasicStroke)stroke);
			outputAttributes(sd, STROKE_ATTRIBUTES);
		} else if (stroke != null) {
			this.strokeOverride = stroke;
		} else {
			set(SVG_STROKE_WIDTH_ATTRIBUTE, "0");
		}
		
		// override with the raphael dasharray version
		if (dashArray != null) {
			this.attr.put("stroke-dasharray", dashArray);
		}
	}
	
	public ShapeStyle(SVGHelper h, ShapeStyle borderStyle) {
		super(h, borderStyle);
		this.invisible = (borderStyle == null)  ? false : borderStyle.invisible;
		this.strokeOverride = (borderStyle == null) ? null : borderStyle.strokeOverride;
	}
	
	public Paint convertPaint(boolean fill) {
		if (fill) {
			Value v = SVGHelper.getCSSStyleProperty(getStyleElement(), CSSConstants.CSS_FILL_PROPERTY);
			if (v instanceof GradientValue) {
				return getHelper().getGradientPaintValueManager().convert(v);
			}
		} 
		
		// css style
		Element e = getStyleElement();
		ShapeNode sn = new ShapeNode();
		sn.setShape(new Rectangle2D.Double(1,1,1,1));
		
		return fill ? PaintServer.convertFillPaint(e, sn, getHelper().getBridge()) : PaintServer.convertStrokePaint(e, sn, getHelper().getBridge());			
	}


	
	
	public void convertPaint(Paint p, boolean fill) {
		if (p instanceof LinearGradientPaint) {
			String out = getHelper().getGradientPaintValueManager().convert((LinearGradientPaint)p);
			attr.put(fill ? "fill" : "stroke", "\""+out+"\"");
		} else {
			final SVGPaint paintConverter = new SVGPaint(getHelper().getContext());
			SVGPaintDescriptor pd = paintConverter.toSVG(p);
			outputAttributes(pd, fill ? SVG_FILL_ATTRIBUTE : SVG_STROKE_ATTRIBUTE, fill ? SVG_FILL_OPACITY_ATTRIBUTE: SVG_STROKE_OPACITY_ATTRIBUTE);
		}
	}
	

}
