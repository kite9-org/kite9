package org.kite9.diagram.adl;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.AbstractStyleableXMLElement;
import org.kite9.framework.xml.Kite9XMLElement;
import org.w3c.dom.Element;

/**
 * This is the base class for connections within the diagram.  i.e. Links.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractXMLConnectionElement extends AbstractStyleableXMLElement {

	private static final long serialVersionUID = -1941426216200603569L;
	
	/**
	 * For serialization
	 */
	public AbstractXMLConnectionElement() {
	}
	
	/**
	 * Call this with modify verteConnected false to avoid adding the edge connection to the vertex
	 */
	public AbstractXMLConnectionElement(String id, String tag, Kite9XMLElement from, Kite9XMLElement to, Direction drawDirection, String fromDecoration, Kite9XMLElement fromLabel, String toDecoration, Kite9XMLElement tolabel, ADLDocument doc) {
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
		
		doc.addConnection(this);
		
	}

	public Kite9XMLElement getFromLabel() {
		return getProperty("fromLabel");
	}

	public Kite9XMLElement getToLabel() {
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

	public void setFromLabel(Kite9XMLElement fromLabel) {
	    replaceProperty("fromLabel", fromLabel);
	}

	public void setToLabel(Kite9XMLElement toLabel) {
	    replaceProperty("toLabel", toLabel);
	}
	
	public Direction getDrawDirection() {
		String dd = getAttribute("drawDirection");
		if (dd.length() == 0) {
			return null;
		}
		return Direction.valueOf(dd);
	}
	
	public Kite9XMLElement getFrom() {
		Element fromEl = getProperty("from");
		String reference = fromEl.getAttribute("reference");
		Kite9XMLElement from = (Kite9XMLElement) ownerDocument.getChildElementById(ownerDocument, reference);
		return from;
	}

	public Kite9XMLElement getTo() {
		Element toEl = getProperty("to");
		String reference = toEl.getAttribute("reference");
		Kite9XMLElement to = (Kite9XMLElement) ownerDocument.getChildElementById(ownerDocument, reference);
		return to;
	}

	public void setFrom(Kite9XMLElement v) {
		Kite9XMLElement from = (Kite9XMLElement) ownerDocument.createElement("from");
		from.setAttribute("reference", v.getID());
		replaceProperty("from", from);
		from = v;
	}

	public void setTo(Kite9XMLElement v) {
		Kite9XMLElement to = (Kite9XMLElement) ownerDocument.createElement("to");
		to.setAttribute("reference", v.getID());
		replaceProperty("to", to);
		to = v;
	}

	public void setDrawDirection(Direction d) {
		if (d == null) {
			removeAttribute("drawDirection");
		} else {
			setAttribute("drawDirection", d.toString());
		}
	}

}