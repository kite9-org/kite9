package org.kite9.diagram.batik.bridge;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Responsible for outputting a class of diagram elements.
 * 
 * Painters are usable by a single component only, and shouldn't be shared across elements.
 * This means they can store temporary state, however, they should follow the contract of the 
 * DiagramElements they are attached to, and be pure.
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public interface Painter<X extends DiagramElement> {
	
	Element output(Document d, StyledKite9SVGElement theElement, X r);

}
