package org.kite9.diagram.dom.painter;

import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.processors.copier.Kite9ExpandingCopier;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for painter implementations where we are simply copying some XML from the 
 * source to the destination.
 */
public class DirectSVGGroupPainter extends AbstractPainter {

	private StyledKite9XMLElement theElement;
	private boolean performedPreprocess = false;
	private XMLProcessor processor;

	
	public DirectSVGGroupPainter(StyledKite9XMLElement theElement, XMLProcessor processor) {
		this.theElement = theElement;
		this.processor = processor;
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
		handleTemporaryElements(out, d);
		new Kite9ExpandingCopier("", out).processContents(in);
		
	}
	
	/**
	 * Use this method to decorate the contents before processing.
	 */
	public StyledKite9XMLElement getContents() {		
		if (theElement == null) {
			throw new Kite9ProcessingException("Painter xml element not set");
		}
		
		if (r == null) {
			throw new Kite9ProcessingException("Painter diagram element not set");
		}
		
		if (performedPreprocess) {
			return theElement;
		}
		
		setupElementXML(theElement);
		processor.processContents(theElement);
		performedPreprocess = true;
		
		return theElement;
	}
	

	/**
	 * Ensures that the element has the correct contents before the pre-processor is called.
	 */
	protected void setupElementXML(StyledKite9XMLElement e) {
	}
}
