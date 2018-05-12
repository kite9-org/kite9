package org.kite9.diagram.dom.elements;

import org.kite9.diagram.model.DiagramElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Useful helper methods for inspecting Kite9 XML elements.
 * 
 * @author robmoffat
 *
 */
public interface Kite9XMLElement extends Element, Iterable<Kite9XMLElement> {
	
	public String getID();
	
	public int getChildXMLElementCount();
	
	public DiagramElement getDiagramElement();
	
	public ADLDocument getOwnerDocument();

	public <E extends Element> E getProperty(String name);
	
	public Element output(Document d);
}
