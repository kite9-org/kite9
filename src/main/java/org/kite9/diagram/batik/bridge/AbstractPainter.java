package org.kite9.diagram.batik.bridge;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;

/**
 * Base class for painter implementations
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public abstract class AbstractPainter<X extends DiagramElement> implements Painter<X> {

	public AbstractPainter() {
		super();
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

	private StyledKite9SVGElement source;
	
	protected StyledKite9SVGElement getContents(StyledKite9SVGElement source, X r) {
		if (this.source == null) {
			this.source = initializeSourceContents(source, r);
		} 
		
		return this.source;
	}
	
	/**
	 * Called where we need to set up the XML content for the element in advance to processing it.
	 */
	protected StyledKite9SVGElement initializeSourceContents(StyledKite9SVGElement source, @SuppressWarnings("unused") X r) {
		return source;
	}

}