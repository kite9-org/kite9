package org.kite9.diagram.dom.model;

import org.kite9.diagram.model.DiagramElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Means this {@link DiagramElement} can output XML
 * representing itself.
 * 
 * @author robmoffat
 *
 */
public interface HasSVGRepresentation extends DiagramElement {
	
	/**
	 * Creates SVG elements representing this DiagramElement and anything 
	 * nested within it.
	 */
	public Element output(Document d);

}
