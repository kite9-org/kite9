package org.kite9.diagram.dom.painter;

import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.processors.copier.Kite9ExpandingCopier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for painter implementations where we are simply copying some XML from the 
 * source to the destination.
 */
public class DirectSVGGroupPainter extends AbstractPainter {

	public DirectSVGGroupPainter(StyledKite9XMLElement theElement, XMLProcessor processor) {
		super(theElement, processor);
	}

	/**
	 * The basic output approach is to turn any DiagramElement into a <g> tag, with the same ID set
	 * as the DiagramElement.  
	 */
	public final Element output(Document d) {
		StyledKite9XMLElement toUse = getContents();
		Element out = d.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
		processOutput(toUse, out, d);
		addAttributes(toUse, out);
		return out;
	}
	

	protected void processOutput(StyledKite9XMLElement in, Element out, @SuppressWarnings("unused") Document d) {
		new Kite9ExpandingCopier("", out).processContents(in);
	}
}
