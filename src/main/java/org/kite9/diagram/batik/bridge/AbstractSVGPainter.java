package org.kite9.diagram.batik.bridge;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.kite9.diagram.batik.templater.Kite9ExpandingCopier;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractSVGPainter<X extends DiagramElement> implements Painter<X> {

	/**
	 * The basic output approach is to turn any DiagramElement into a <g> tag, with the same ID set
	 * as the DiagramElement.  
	 */
	@Override
	public Element output(Document d, StyledKite9SVGElement theElement, X r) {
		Element out = d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12OMDocument.SVG_G_TAG);
		processContents(theElement, out, d, r);
		out.setAttribute(SVG12OMDocument.SVG_ID_ATTRIBUTE, r.getID());
		String clazz = theElement.getCSSClass().trim();
		if (clazz.length() > 0) {
			out.setAttribute("class", clazz);
		}
		
		String style = theElement.getAttribute("style");
		if (style.length() > 0) {
			out.setAttribute("style", style);
		}
		return out;
	}

	protected void processContents(StyledKite9SVGElement in, Element out, @SuppressWarnings("unused") Document d, @SuppressWarnings("unused") X z) {
		new Kite9ExpandingCopier("", out).processContents(in);
	}
}
