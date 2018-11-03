package org.kite9.diagram.dom.painter;

import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.processors.copier.Kite9ExpandingCopier;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for painter implementations where we are simply copying some XML from the 
 * source to the destination.
 */
public class DirectSVGGroupPainter extends AbstractPainter {

	public DirectSVGGroupPainter(StyledKite9SVGElement theElement, Kite9BridgeContext context) {
		super(theElement, context.getXMLPreProcessor());
	}

	/**
	 * The basic output approach is to turn any DiagramElement into a <g> tag, with the same ID set
	 * as the DiagramElement.  
	 */
	public final Element output(Document d) {
		StyledKite9SVGElement toUse = getContents();
		Element out = d.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_G_TAG);
		setGroupAttributes(toUse, out);
		processOutput(toUse, out, d);
		return out;
	}
	

	protected void processOutput(StyledKite9SVGElement in, Element out, @SuppressWarnings("unused") Document d) {
		new Kite9ExpandingCopier("", out).processContents(in);
	}
}
