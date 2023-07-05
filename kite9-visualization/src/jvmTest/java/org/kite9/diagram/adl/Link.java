package org.kite9.diagram.adl;

import org.kite9.diagram.dom.css.CSSConstants;

import org.kite9.diagram.model.position.Direction;
import org.w3c.dom.Element;
import org.w3c.dom.*;

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
	
	public static final String LINK_TEST = "link-test";

	private static final long serialVersionUID = -5950978530304852748L;

	public Link() {
		this.tagName = "link";
	}
	
	public Link(Element from, Element to) {
		this(getID(from)+"-"+getID(to), from, to, TESTING_DOCUMENT);
	}
		
	public Link(String id, Element from, Element to, Document doc) {
		this(id, from, to, null, null, null, null, null, doc);
	}
	
	public Link(Element from, Element to, String fromStyle, Element fromLabel, String toEndStyle, Element toLabel, Direction drawDirection) {
		this(getID(from)+"-"+getID(to), from, to, fromStyle, fromLabel, toEndStyle, toLabel, drawDirection, TESTING_DOCUMENT);
	}
	
	public Link(String id, Element from, Element to, String fromStyle, Element fromLabel, String toEndStyle, Element toLabel, Direction drawDirection, Document doc) {
		super(id, "link", from, to, drawDirection, fromStyle, fromLabel, toEndStyle, toLabel, doc);
		setAttribute("style", CSSConstants.LINK_CORNER_RADIUS+": 5px; ");
	}

	public Link(Element from, Element to, String fromStyle, Element fromLabel, String toStyle, Element toLabel) {
		super(getID(from)+"-"+getID(to), "link",  from, to, null, fromStyle, fromLabel, toStyle, toLabel, TESTING_DOCUMENT);
		setAttribute("style", CSSConstants.LINK_CORNER_RADIUS+": 5px; ");
	}

	public Link(String id, Element from, Element to) {
		this(id, from, to, TESTING_DOCUMENT);
	}

	public Link(String id, Element from, Element to, String fromStyle, Element fromLabel, String toStyle, Element toLabel, Direction d) {
		this(id, from, to, fromStyle, fromLabel, toStyle, toLabel, d,  TESTING_DOCUMENT);
	}

	/**
	 * Contains the ordering of the field within the diagram allLinks() list.
	 */
	public int getRank() {
		String out = getAttribute("rank");
		if ("".equals(out)) {
			return 0;
		} else {
			return Integer.parseInt(out);
		}
	}

	public void setRank(int rank) {
		setAttribute("rank", ""+rank);
	}

	@Override
	protected Node newNode() {
		return new Link();
	}
	
	
}
