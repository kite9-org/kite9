package org.kite9.diagram.adl;

import org.kite9.diagram.position.Direction;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.XMLElement;
import org.w3c.dom.Node;

/**
 * Joins glyphs and arrows to one another. 
 * Can have text attached to the 'to' end, and decoration at the 'to' end
 * 
 * Set the drawDirection if the rendering system should draw the link in a particular orientation on the
 * diagram.
 * 
 * @author robmoffat
 */
public class Link extends AbstractXMLConnectionElement {

	private static final long serialVersionUID = -5950978530304852748L;

	public Link() {
		this.tagName = "link";
	}
	
	public Link(XMLElement from, XMLElement to) {
		this(from.getID()+"-"+to.getID(), from, to, TESTING_DOCUMENT);
	}
		
	public Link(String id, XMLElement from, XMLElement to, ADLDocument doc) {
		this(id, from, to, null, null, null, null, null, doc);
	}
	
	public Link(XMLElement from, XMLElement to, String fromStyle, XMLElement fromLabel, String toEndStyle, XMLElement toLabel, Direction drawDirection) {
		this(from.getID()+"-"+to.getID(), from, to, fromStyle, fromLabel, toEndStyle, toLabel, drawDirection, TESTING_DOCUMENT);
	}
	
	public Link(String id, XMLElement from, XMLElement to, String fromStyle, XMLElement fromLabel, String toEndStyle, XMLElement toLabel, Direction drawDirection, ADLDocument doc) {
		super(id, "link", from, to, drawDirection, fromStyle, fromLabel, toEndStyle, toLabel, doc);
	}

	public Link(XMLElement from, XMLElement to, String fromStyle, XMLElement fromLabel, String toStyle, XMLElement toLabel) {
		super(from.getID()+"-"+to.getID(), "link",  from, to, null, null, fromLabel, null, toLabel, TESTING_DOCUMENT);
	}

	@Override
	public XMLElement getFromDecoration() {
		return getProperty("fromDecoration");
	}

	@Override
	public XMLElement getToDecoration() {
		return getProperty("toDecoration");
	}
	
	private void setDecoration(String name, Object d) {
		XMLElement e = (XMLElement) ownerDocument.createElement(name);
		e.setTextContent((String) d);
		replaceProperty(name, e);
	}

	@Override
	public void setFromDecoration(XMLElement fromDecoration) {
		replaceProperty("fromDecoration", fromDecoration);
	}

	@Override
	public void setToDecoration(XMLElement toDecoration) {
		replaceProperty("toDecoration", toDecoration);
	}

	/**
	 * Contains the ordering of the field within the diagram allLinks() list.
	 */
	int rank;

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	protected Node newNode() {
		return new Link();
	}
	
	
}