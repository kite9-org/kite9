package org.kite9.framework.xml;

import org.kite9.diagram.batik.element.Templater;
import org.kite9.diagram.model.DiagramElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Useful helper methods for creating and inspecting Kite9 SVG documents, mainly used in testing.
 * 
 * @author robmoffat
 *
 */
public interface Kite9XMLElement extends Element, Iterable<Kite9XMLElement> {
	
	public String getID();

	public void setTagName(String tag);
	
	public void setOwnerDocument(ADLDocument doc);
	
	public int getChildXMLElementCount();
	
	public DiagramElement getDiagramElement();
	
	public ADLDocument getOwnerDocument();

	public <E extends Element> E getProperty(String name);
	
	public <E extends Element> E replaceProperty(String propertyName, E e);
	
	public Element output(Document d, Templater t);
}
