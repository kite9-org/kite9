package org.kite9.diagram.dom.painter;

import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for painter implementations where we are simply copying some XML from the 
 * source to the destination.
 */
public class DirectSVGGroupPainter extends AbstractPainter {

	protected Element theElement;
	
	public DirectSVGGroupPainter(Element theElement) {
		this.theElement = theElement;
	}
	
	/**
	 * The basic output approach is to turn any DiagramElement into a <g> tag, with the same ID set
	 * as the DiagramElement.  
	 */
	public Element output(Document d, XMLProcessor postProcessor) {
		Element out = processOutput(theElement, d, postProcessor);
		if (out != null) {
			addAttributes(theElement, out);
		}
		return out;
	}
	

	protected Element processOutput(Element in, Document d, XMLProcessor postProcessor) {
		Element out = (Element) postProcessor.processContents(in);
		handleTemporaryElements(out, d, postProcessor);
		return out;
	}

	/**
	 * Ensures that the element has the correct contents before the pre-processor is called.
	 */
	protected void setupElementXML(Element e) {
	}
}
