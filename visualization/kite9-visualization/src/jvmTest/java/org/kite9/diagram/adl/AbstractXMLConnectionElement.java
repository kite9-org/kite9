package org.kite9.diagram.adl;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is the base class for connections within the diagram.  i.e. Links.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractXMLConnectionElement extends AbstractMutableXMLElement {

	private static final long serialVersionUID = -1941426216200603569L;
	
	/**
	 * For serialization
	 */
	public AbstractXMLConnectionElement() {
	}
	
	/**
	 * Call this with modify verteConnected false to avoid adding the edge connection to the vertex
	 */
	public AbstractXMLConnectionElement(String id, String tag, Element from, Element to, Direction drawDirection, String fromDecoration, Element fromLabel, String toDecoration, Element tolabel, Document doc) {
		super(id, tag, doc);
		setFrom(from);
		setTo(to);
		setDrawDirection(drawDirection);
		
		if (fromDecoration != null) {
			setFromDecoration(fromDecoration);
		}

		if (toDecoration != null) {
			setToDecoration(toDecoration);
		}
		
		if (fromLabel!=null) {
			setFromLabel(fromLabel);
		}
		
		if (tolabel!=null) { 
			setToLabel(tolabel);
		}

		AbstractMutableXMLElement.CONNECTION_ELEMENTS.add(this);
	}

	public Element getFromLabel() {
		return getProperty("fromLabel");
	}

	public Element getToLabel() {
		return getProperty("toLabel");
	}

	public int compareTo(DiagramElement o) {
		if (o!=null) {
			return this.toString().compareTo(o.toString());
		} else {
			return -1;
		}
	}

	public void setFromDecoration(String fromDecoration) {
		Element fromElement = getProperty("from");
		fromElement.setAttribute("id", createID());
		fromElement.setAttribute("class", fromDecoration.toLowerCase());
	}
	
	public void setToDecoration(String toDecoration) {
		Element fromElement = getProperty("to");
		fromElement.setAttribute("id", createID());
		fromElement.setAttribute("class", toDecoration.toLowerCase());
	}

	public void setFromLabel(Element fromLabel) {
	    replaceProperty("fromLabel", fromLabel);
	}

	public void setToLabel(Element toLabel) {
	    replaceProperty("toLabel", toLabel);
	}
	
	public Direction getDrawDirection() {
		String dd = getAttribute("drawDirection");
		if (dd.length() == 0) {
			return null;
		}
		return Direction.valueOf(dd);
	}
	
	public Element getFrom() {
		Element fromEl = getProperty("from");
		String reference = fromEl.getAttribute("reference");
		Element from = (Element) ownerDocument.getChildElementById(ownerDocument, reference);
		return from;
	}

	public Element getTo() {
		Element toEl = getProperty("to");
		String reference = toEl.getAttribute("reference");
		Element to = (Element) ownerDocument.getChildElementById(ownerDocument, reference);
		return to;
	}

	public void setFrom(Element v) {
		GenericMutableXMLElement from = new GenericMutableXMLElement("from", (Document) ownerDocument);
		from.setAttribute("reference", getID(v));
		replaceProperty("from", from);
	}

	public void setTo(Element v) {
		GenericMutableXMLElement to = new GenericMutableXMLElement("to", (Document) ownerDocument);
		to.setAttribute("reference", getID(v));
		replaceProperty("to", to);
	}

	public void setDrawDirection(Direction d) {
		if (d == null) {
			removeAttribute("drawDirection");
		} else {
			setAttribute("drawDirection", d.toString());
		}
	}

}