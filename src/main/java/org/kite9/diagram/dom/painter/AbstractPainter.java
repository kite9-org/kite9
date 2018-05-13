package org.kite9.diagram.dom.painter;

import java.util.Map;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Element;

/**
 * Base class for painter implementations
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public abstract class AbstractPainter implements Painter {

	private StyledKite9SVGElement theElement;
	protected DiagramElement r;
	private Map<String, String> parameters;
	private boolean performedPreprocess = false;
	private XMLProcessor processor;
	
	
	@Override
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public AbstractPainter(StyledKite9SVGElement theElement, XMLProcessor processor) {
		super();
		this.theElement = theElement;
		this.processor = processor;
	}

	@Override
	public void setDiagramElement(DiagramElement de) {
		this.r = de;
	}

	protected void addStyleAndClass(StyledKite9SVGElement in, Element out) {
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
//	
//	protected void addAttributes(StyledKite9SVGElement in, X r, Element out) {
//		NamedNodeMap nnm = in.getAttributes();
//		for (int i = 0; i < nnm.getLength(); i++) {
//			Attr a = (Attr) nnm.item(i);
//			out.setAttribute(a.getNodeName(), a.getNodeValue());
//		}
//	}
	

	protected void addAttributes(StyledKite9SVGElement toUse, Element out) {
		String id = r.getID();
		if (id.length() > 0) {
			out.setAttribute("id", id);
		}
		out.setAttribute("kite9-elem", toUse.getTagName());
	}

	/**
	 * Use this method to decorate the contents before processing.
	 */
	public StyledKite9SVGElement getContents() {
		if (parameters == null) {
			throw new Kite9ProcessingException("Painter parameters not set");
		}
		
		if (theElement == null) {
			throw new Kite9ProcessingException("Painter xml element not set");
		}
		
		if (r == null) {
			throw new Kite9ProcessingException("Painter diagram element not set");
		}
		
		if (performedPreprocess) {
			return theElement;
		}
		
		processor.processContents(theElement);
		performedPreprocess = true;
		
		return theElement;
	}
	
}