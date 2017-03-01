package org.kite9.diagram.visualization.format.svg;

import static org.apache.batik.util.SVGConstants.*;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.svggen.DefaultExtensionHandler;
import org.apache.batik.svggen.SVGColor;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPaintDescriptor;
import org.kite9.diagram.visualization.display.complete.TransformedPaint;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Element;

/**
 * Extension of Batik's {@link DefaultExtensionHandler} which handles different
 * kinds of Paint objects
 * 
 * I wonder why this is not part of the svggen library.
 * 
 * @author Martin Steiger
 */
public class GradientExtensionHandler extends DefaultExtensionHandler {
	
	Map<String, SVGPaintDescriptor> paintMap = new HashMap<String, SVGPaintDescriptor>();

	@Override
	public SVGPaintDescriptor handlePaint(Paint paint, SVGGeneratorContext genCtx) {
		boolean percentage = false;

		if (paint instanceof TransformedPaint) {
			String key = ((TransformedPaint) paint).getKey();
			paint = ((TransformedPaint) paint).getUnderlyingPaint();
			percentage = true;

			if (paintMap.containsKey(key)) {
				return paintMap.get(key);
			}
		}

		SVGPaintDescriptor out = null;

		if (paint instanceof LinearGradientPaint) {
			out = getLgpDescriptor((LinearGradientPaint) paint, genCtx, percentage);
		} else if (paint instanceof RadialGradientPaint) {
			out = getRgpDescriptor((RadialGradientPaint) paint, genCtx, percentage);
		} 

		if (paint instanceof TransformedPaint) {
			String key = ((TransformedPaint) paint).getKey();
			paintMap.put(key, out);
		}

		if (out != null) {
			return out;
		}

		return super.handlePaint(paint, genCtx);

	}

	private SVGPaintDescriptor getRgpDescriptor(RadialGradientPaint gradient, SVGGeneratorContext genCtx, boolean percentage) {
		Element gradElem = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_RADIAL_GRADIENT_TAG);

		// Create and set unique XML id
		String id = genCtx.getIDGenerator().generateID("gradient");
		gradElem.setAttribute(SVG_ID_ATTRIBUTE, id);

		// Set x,y pairs
		Point2D centerPt = gradient.getCenterPoint();
		gradElem.setAttribute("cx", convertCoordinate(centerPt.getX(), percentage));
		gradElem.setAttribute("cy", convertCoordinate(centerPt.getY(), percentage));

		Point2D focusPt = gradient.getFocusPoint();
		gradElem.setAttribute("fx", convertCoordinate(focusPt.getX(), percentage));
		gradElem.setAttribute("fy", convertCoordinate(focusPt.getY(), percentage));

		gradElem.setAttribute("r", convertCoordinate(gradient.getRadius(), percentage));

		addMgpAttributes(gradElem, genCtx, gradient, percentage);

		return new SVGPaintDescriptor("url(#" + id + ")", SVG_OPAQUE_VALUE, gradElem);
	}

	private SVGPaintDescriptor getLgpDescriptor(LinearGradientPaint gradient, SVGGeneratorContext genCtx, boolean percentage) {
		Element gradElem = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_LINEAR_GRADIENT_TAG);

		// Create and set unique XML id
		String id = genCtx.getIDGenerator().generateID("gradient");
		gradElem.setAttribute(SVG_ID_ATTRIBUTE, id);

		// Set x,y pairs
		Point2D startPt = gradient.getStartPoint();
		gradElem.setAttribute("x1", convertCoordinate(startPt.getX(), percentage));
		gradElem.setAttribute("y1", convertCoordinate(startPt.getY(), percentage));

		Point2D endPt = gradient.getEndPoint();
		gradElem.setAttribute("x2", convertCoordinate(endPt.getX(), percentage));
		gradElem.setAttribute("y2", convertCoordinate(endPt.getY(), percentage));

		addMgpAttributes(gradElem, genCtx, gradient, percentage);

		return new SVGPaintDescriptor("url(#" + id + ")", SVG_OPAQUE_VALUE, gradElem);
	}

	private String convertCoordinate(double p, boolean percentage) {
		if (percentage) {
			return String.valueOf(p * 100)+"%";
		} else {
			return String.valueOf(p);
		}
	}

	private void addMgpAttributes(Element gradElem, SVGGeneratorContext genCtx, MultipleGradientPaint gradient, boolean percentage) {
		if (percentage) {
			
		} else {
			gradElem.setAttribute(SVG_GRADIENT_UNITS_ATTRIBUTE, SVG_USER_SPACE_ON_USE_VALUE);
		}

		// Set cycle method
		switch (gradient.getCycleMethod()) {
		case REFLECT:
			gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, SVG_REFLECT_VALUE);
			break;
		case REPEAT:
			gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, SVG_REPEAT_VALUE);
			break;
		case NO_CYCLE:
			gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, SVG_PAD_VALUE); // this
																				// is
																				// the
																				// default
			break;
		}

		// Set color space
		switch (gradient.getColorSpace()) {
		case LINEAR_RGB:
			gradElem.setAttribute(SVG_COLOR_INTERPOLATION_ATTRIBUTE, SVG_LINEAR_RGB_VALUE);
			break;

		case SRGB:
			gradElem.setAttribute(SVG_COLOR_INTERPOLATION_ATTRIBUTE, SVG_SRGB_VALUE);
			break;
		}

		// Set transform matrix if not identity
		AffineTransform tf = gradient.getTransform();
		if (!tf.isIdentity()) {
			String matrix = "matrix(" + tf.getScaleX() + " " + tf.getShearX() + " " + tf.getTranslateX() + " " + tf.getScaleY() + " " + tf.getShearY() + " " + tf.getTranslateY() + ")";
			gradElem.setAttribute(SVG_TRANSFORM_ATTRIBUTE, matrix);
		}

		// Convert gradient stops
		Color[] colors = gradient.getColors();
		float[] fracs = gradient.getFractions();

		for (int i = 0; i < colors.length; i++) {
			Element stop = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_STOP_TAG);
			SVGPaintDescriptor pd = SVGColor.toSVG(colors[i], genCtx);

			stop.setAttribute(SVG_OFFSET_ATTRIBUTE, (int) (fracs[i] * 100.0f) + "%");
			stop.setAttribute(SVG_STOP_COLOR_ATTRIBUTE, pd.getPaintValue());

			if (colors[i].getAlpha() != 255) {
				stop.setAttribute(SVG_STOP_OPACITY_ATTRIBUTE, pd.getOpacityValue());
			}

			gradElem.appendChild(stop);
		}
	}
}