package org.kite9.diagram.batik.bridge;

import java.awt.geom.Rectangle2D;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.kite9.diagram.batik.HasSVGGraphics;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Handles painting for {@link DiagramElementType.CONTAINER}
 * 
 * @author robmoffat
 *
 */
public class ContainerRectangularPainter implements RectangularPainter<Container> {

	@Override
	public Element output(Document d, StyledKite9SVGElement theElement, Container r) {
		Element out = d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12OMDocument.SVG_G_TAG);
		out.setAttribute(SVG12OMDocument.SVG_ID_ATTRIBUTE, r.getID());
		out.setAttribute("class", theElement.getCSSClass());
		out.setAttribute("style", theElement.getAttribute("style"));
		
		for (DiagramElement de : r.getContents()) {
			if (de instanceof HasSVGGraphics) {
				Element deOut = ((HasSVGGraphics) de).output(d);
				out.appendChild(deOut);
			}
		}
		
		return out;
	}

	/**
	 * Bounds for rectangles is special - it should be the minimum size possible.
	 */
	@Override
	public Rectangle2D bounds(Element in) {
		throw new Kite9ProcessingException("No fixed bounds for Container, it depends on contents: " + in);
	}

}
