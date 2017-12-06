package org.kite9.diagram.adl;

import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.Kite9XMLElement;
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
	
	public static final String LINK_TEST = "link-test";

	private static final long serialVersionUID = -5950978530304852748L;

	public Link() {
		this.tagName = "link";
	}
	
	public Link(Kite9XMLElement from, Kite9XMLElement to) {
		this(from.getID()+"-"+to.getID(), from, to, TESTING_DOCUMENT);
	}
		
	public Link(String id, Kite9XMLElement from, Kite9XMLElement to, ADLDocument doc) {
		this(id, from, to, null, null, null, null, null, doc);
	}
	
	public Link(Kite9XMLElement from, Kite9XMLElement to, String fromStyle, Kite9XMLElement fromLabel, String toEndStyle, Kite9XMLElement toLabel, Direction drawDirection) {
		this(from.getID()+"-"+to.getID(), from, to, fromStyle, fromLabel, toEndStyle, toLabel, drawDirection, TESTING_DOCUMENT);
	}
	
	public Link(String id, Kite9XMLElement from, Kite9XMLElement to, String fromStyle, Kite9XMLElement fromLabel, String toEndStyle, Kite9XMLElement toLabel, Direction drawDirection, ADLDocument doc) {
		super(id, "link", from, to, drawDirection, fromStyle, fromLabel, toEndStyle, toLabel, doc);
	}

	public Link(Kite9XMLElement from, Kite9XMLElement to, String fromStyle, Kite9XMLElement fromLabel, String toStyle, Kite9XMLElement toLabel) {
		super(from.getID()+"-"+to.getID(), "link",  from, to, null, fromStyle, fromLabel, toStyle, toLabel, TESTING_DOCUMENT);
	}

	public Link(String id, Kite9XMLElement from, Kite9XMLElement to) {
		this(id, from, to, TESTING_DOCUMENT);
	}

	public Link(String id, Kite9XMLElement from, Kite9XMLElement to, String fromStyle, Kite9XMLElement fromLabel, String toStyle, Kite9XMLElement toLabel, Direction d) {
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
