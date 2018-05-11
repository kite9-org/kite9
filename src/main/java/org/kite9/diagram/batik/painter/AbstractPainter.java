package org.kite9.diagram.batik.painter;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.model.DiagramElement;
import org.w3c.dom.Element;

/**
 * Base class for painter implementations
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public abstract class AbstractPainter implements Painter {

	protected StyledKite9SVGElement theElement;
	protected DiagramElement r;
	
	public AbstractPainter(StyledKite9SVGElement theElement) {
		super();
		this.theElement = theElement;
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

	protected StyledKite9SVGElement getContents() {
		return theElement;
	}
	
}