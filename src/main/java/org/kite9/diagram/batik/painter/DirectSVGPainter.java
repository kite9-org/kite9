package org.kite9.diagram.batik.painter;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.kite9.diagram.batik.templater.Kite9ExpandingCopier;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for painter implementations where we are simply copying some XML from the 
 * source to the destination.
 */
public class DirectSVGPainter<X extends DiagramElement> extends AbstractPainter<X> implements Painter<X> {

	/**
	 * The basic output approach is to turn any DiagramElement into a <g> tag, with the same ID set
	 * as the DiagramElement.  
	 */
	public final Element output(Document d, StyledKite9SVGElement theElement, X r) {
		StyledKite9SVGElement toUse = getContents(theElement, r);
		Element out = d.createElementNS(SVG12OMDocument.SVG_NAMESPACE_URI, SVG12OMDocument.SVG_G_TAG);
		processOutput(toUse, out, d, r);
		addAttributes(toUse, r, out);
		return out;
	}
	

	protected void processOutput(StyledKite9SVGElement in, Element out, @SuppressWarnings("unused") Document d, @SuppressWarnings("unused") X z) {
		new Kite9ExpandingCopier("", out).processContents(in);
	}
}
