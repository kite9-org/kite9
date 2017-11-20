package org.kite9.diagram.batik.bridge;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.kite9.diagram.batik.templater.Kite9ExpandingCopier;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.common.Kite9ProcessingException;
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
		initializeSourceContents(theElement, r);
		Element out = d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12OMDocument.SVG_G_TAG);
		processOutput(theElement, out, d, r);
		addStyleAndClass(theElement, r, out);
		return out;
	}

	protected void addStyleAndClass(StyledKite9SVGElement in, X r, Element out) {
		out.setAttribute(SVG12OMDocument.SVG_ID_ATTRIBUTE, r.getID());
		String clazz = in.getCSSClass().trim();
		if (clazz.length() > 0) {
			out.setAttribute("class", clazz);
		}
		
		String style = in.getAttribute("style");
		if (style.length() > 0) {
			out.setAttribute("style", style);
		}
	}

	StyledKite9SVGElement source;
	
	/**
	 * Called where we need to set up the XML content for the element in advance to processing it.
	 */
	protected void initializeSourceContents(StyledKite9SVGElement source, @SuppressWarnings("unused") X r) {
		if (this.source == null) {
			this.source = source;
		} else if (this.source != source) {
			throw new Kite9ProcessingException("have reused painter");
		}
	}

	protected void processOutput(StyledKite9SVGElement in, Element out, @SuppressWarnings("unused") Document d, @SuppressWarnings("unused") X z) {
		new Kite9ExpandingCopier("", out).processContents(in);
	}
}
