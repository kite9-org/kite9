package org.kite9.diagram.batik.format;

import static org.apache.batik.util.SVGConstants.SVG_GRADIENT_UNITS_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_G_TAG;
import static org.apache.batik.util.SVGConstants.SVG_HEIGHT_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_ID_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_LINEAR_GRADIENT_TAG;
import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;
import static org.apache.batik.util.SVGConstants.SVG_OFFSET_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_OPAQUE_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_PATTERN_TAG;
import static org.apache.batik.util.SVGConstants.SVG_PATTERN_UNITS_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_RADIAL_GRADIENT_TAG;
import static org.apache.batik.util.SVGConstants.SVG_STOP_COLOR_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STOP_OPACITY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STOP_TAG;
import static org.apache.batik.util.SVGConstants.SVG_TRANSFORM_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_USER_SPACE_ON_USE_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_WIDTH_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_X_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_Y_ATTRIBUTE;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.batik.bridge.SVGPatternElementBridge.PatternGraphicsNode;
import org.apache.batik.ext.awt.LinearGradientPaint;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.RadialGradientPaint;
import org.apache.batik.gvt.PatternPaint;
import org.apache.batik.svggen.DefaultExtensionHandler;
import org.apache.batik.svggen.SVGColor;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPaintDescriptor;
import org.w3c.dom.Element;

/**
 * Extension of Batik's {@link DefaultExtensionHandler} which handles different
 * kinds of Paint objects
 * 
 * I wonder why this is not part of the svggen library.
 * 
 * @author Martin Steiger
 */
public class BatikPaintExtensionHandler extends DefaultExtensionHandler {
	
	Map<String, SVGPaintDescriptor> paintMap = new HashMap<String, SVGPaintDescriptor>();

	@Override
	public SVGPaintDescriptor handlePaint(Paint paint, SVGGeneratorContext genCtx) {
		boolean percentage = true;

		Paint paint2 = paint;
		if (paint2 instanceof TransformedPaint) {
			String key = ((TransformedPaint) paint2).getKey();
			paint2 = ((TransformedPaint) paint2).getUnderlyingPaint();
			percentage = true;

			if (paintMap.containsKey(key)) {
				return paintMap.get(key);
			}
		}

		SVGPaintDescriptor out = null;

		if (paint2 instanceof LinearGradientPaint) {
			out = getLgpDescriptor((LinearGradientPaint) paint2, genCtx, percentage);
		} else if (paint2 instanceof RadialGradientPaint) {
			out = getRgpDescriptor((RadialGradientPaint) paint2, genCtx, percentage);
		} else if (paint2 instanceof PatternPaint) {
			out = getPatternDescriptor((PatternPaint) paint2, genCtx);
		}

		if (paint2 instanceof TransformedPaint) {
			String key = ((TransformedPaint) paint2).getKey();
			paintMap.put(key, out);
		}

		if (out != null) {
			return out;
		}

		return super.handlePaint(paint2, genCtx);

	}

	private SVGPaintDescriptor getPatternDescriptor(PatternPaint paint, SVGGeneratorContext genCtx) {
		String id = genCtx.getIDGenerator().generateID("pattern");

		// set up the pattern element
		Element patternElem = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_PATTERN_TAG);
		Rectangle2D rect = paint.getPatternRect();
		patternElem.setAttribute(SVG_ID_ATTRIBUTE, id);
		patternElem.setAttribute(SVG_PATTERN_UNITS_ATTRIBUTE, SVG_USER_SPACE_ON_USE_VALUE);
		patternElem.setAttribute(SVG_X_ATTRIBUTE, ""+rect.getX());
		patternElem.setAttribute(SVG_Y_ATTRIBUTE, ""+rect.getY());
		patternElem.setAttribute(SVG_WIDTH_ATTRIBUTE, ""+rect.getWidth());
		patternElem.setAttribute(SVG_HEIGHT_ATTRIBUTE, ""+rect.getHeight());

		// paint the pattern inside a group element
		PatternGraphicsNode patternNode = (PatternGraphicsNode) paint.getGraphicsNode();
		Element groupElem = genCtx.getDOMFactory().createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
		ExtendedSVGGraphics2D esvg = new ExtendedSVGGraphics2D((ExtendedSVGGeneratorContext) genCtx, groupElem);
		esvg.transform(patternNode.getInverseTransform());
		patternNode.paint(esvg);
		groupElem = esvg.getTopLevelGroup(true);
		
		// add the group to the pattern
		patternElem.appendChild(groupElem);
		
		return new SVGPaintDescriptor("url(#" + id + ")", SVG_OPAQUE_VALUE, patternElem);
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

//		// Set cycle method
//		switch (gradient.getCycleMethod()) {
//		case CycleMethodEnum.REFLECT:
//			gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, SVG_REFLECT_VALUE);
//			break;
//		case REPEAT:
//			gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, SVG_REPEAT_VALUE);
//			break;
//		case NO_CYCLE:
//			gradElem.setAttribute(SVG_SPREAD_METHOD_ATTRIBUTE, SVG_PAD_VALUE); // this
//																				// is
//																				// the
//																				// default
//			break;
//		}
//
//		// Set color space
//		switch (gradient.getColorSpace()) {
//		case LINEAR_RGB:
//			gradElem.setAttribute(SVG_COLOR_INTERPOLATION_ATTRIBUTE, SVG_LINEAR_RGB_VALUE);
//			break;
//
//		case SRGB:
//			gradElem.setAttribute(SVG_COLOR_INTERPOLATION_ATTRIBUTE, SVG_SRGB_VALUE);
//			break;
//		}

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