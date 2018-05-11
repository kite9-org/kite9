package org.kite9.diagram.batik.painter;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.kite9.framework.dom.elements.StyledKite9SVGElement;
import org.kite9.framework.dom.processors.Kite9ExpandingCopier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for painter implementations where we are simply copying some XML from the 
 * source to the destination.
 */
public class DirectSVGPainter extends AbstractPainter {

	public DirectSVGPainter(StyledKite9SVGElement theElement) {
		super(theElement);
	}

	/**
	 * The basic output approach is to turn any DiagramElement into a <g> tag, with the same ID set
	 * as the DiagramElement.  
	 */
	public final Element output(Document d) {
		StyledKite9SVGElement toUse = getContents();
		Element out = d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12OMDocument.SVG_G_TAG);
		processOutput(toUse, out, d);
		addAttributes(toUse, out);
		return out;
	}
	

	protected void processOutput(StyledKite9SVGElement in, Element out, @SuppressWarnings("unused") Document d) {
		new Kite9ExpandingCopier("", out).processContents(in);
	}
}
