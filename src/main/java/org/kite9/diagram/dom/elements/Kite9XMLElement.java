package org.kite9.diagram.dom.elements;

import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.DiagramElementType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base interface for all xml elements which will be represented as kite9 diagram elements.
 * 
 * @author robmoffat
 *
 */
public interface Kite9XMLElement extends Element, Iterable<Kite9XMLElement> {
	
	public String getID();
		
	public DiagramElement getDiagramElement();
	
	/**
	 * Type-safe override
	 */
	public ADLDocument getOwnerDocument();

	public <E extends Element> E getProperty(String name);
	
	public Element output(Document d, XMLProcessor processor);
	
	public DiagramElementType getType();
}
