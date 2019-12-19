package org.kite9.diagram.dom.painter;

import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.processors.post.Kite9ExpandingCopier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for painter implementations where we are simply copying some XML from the 
 * source to the destination.
 */
public class DirectSVGGroupPainter extends AbstractPainter {

	protected StyledKite9XMLElement theElement;
	
	public DirectSVGGroupPainter(StyledKite9XMLElement theElement) {
		this.theElement = theElement;
	}
	
	/**
	 * The basic output approach is to turn any DiagramElement into a <g> tag, with the same ID set
	 * as the DiagramElement.  
	 */
	public Element output(Document d) {
		Element out = d.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
		processOutput(theElement, out, d);
		addAttributes(theElement, out);
		return out;
	}
	

	protected void processOutput(StyledKite9XMLElement in, Element out, @SuppressWarnings("unused") Document d) {
		new Kite9ExpandingCopier("", out).processContents(in);
		handleTemporaryElements(out, d);
	}

	/**
	 * Ensures that the element has the correct contents before the pre-processor is called.
	 */
	protected void setupElementXML(StyledKite9XMLElement e) {
	}
}
