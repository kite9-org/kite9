package org.kite9.diagram.batik.painter;

import org.kite9.diagram.model.DiagramElement;
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
public interface Painter {
	
	Element output(Document d);
	
	void setDiagramElement(DiagramElement de);

}
