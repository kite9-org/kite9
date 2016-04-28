package org.kite9.diagram.visualization.format.pos;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGPath;
import org.kite9.diagram.visualization.display.java2d.style.io.PathConverter;
import org.kite9.framework.logging.LogicException;
import org.w3c.dom.Element;

import com.thoughtworks.xstream.converters.SingleValueConverter;

/**
 * This class handles the conversion of Java2D Shape objects into SVG Path description strings.
 */
public class SVGPathConverter implements PathConverter, SingleValueConverter {

	SVGPath converter;
	
	public SVGPathConverter() {
		try {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
		converter = new SVGPath(SVGGeneratorContext.createDefault(builder.newDocument()));
		} catch (ParserConfigurationException e) {
			throw new LogicException("Shouldn't get this error: ",e);
		}
	}
	
	public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
		return Shape.class.isAssignableFrom(type);
	}

	public String toString(Object obj) {
		if (obj == null) {
			return "";
		}
		Element e = converter.toSVG((Shape) obj);
		return e == null ? "" : e.getAttribute("d");
	}

	public Object fromString(String str) {
		return null;
	}

	@Override
	public String convert(Shape shape, double xo, double yo) {
		AffineTransform at = AffineTransform.getTranslateInstance(-xo, -yo);
		return toString(at.createTransformedShape(shape));
	}

}
