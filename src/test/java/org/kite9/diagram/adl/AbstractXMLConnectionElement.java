package org.kite9.diagram.adl;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.AbstractStyleableXMLElement;
import org.kite9.diagram.xml.XMLElement;
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
	public AbstractXMLConnectionElement(String id, String tag, XMLElement from, XMLElement to, Direction drawDirection, String fromDecoration, XMLElement fromLabel, String toDecoration, XMLElement tolabel, ADLDocument doc) {
		super(id, tag, doc);
		setFrom(from);
		setTo(to);
		setDrawDirection(drawDirection);
		
		if (fromDecoration != null) {
	//		setFromDecoration(new LinkTerminator("fromDecoration", this.getOwnerDocument(), fromDecoration));
		}

		if (toDecoration != null) {
	//		setToDecoration(new LinkTerminator("toDecoration", this.getOwnerDocument(), toDecoration));
		}
		
		if (fromLabel!=null) {
			setFromLabel(fromLabel);
		}
		
		if (tolabel!=null) { 
			setToLabel(tolabel);
		}
	}
	
	public abstract XMLElement getFromDecoration();

	public abstract XMLElement getToDecoration();

	public XMLElement getFromLabel() {
		return getProperty("fromLabel");
	}

	public XMLElement getToLabel() {
		return getProperty("toLabel");
	}

	public int compareTo(DiagramElement o) {
		if (o!=null) {
			return this.toString().compareTo(o.toString());
		} else {
			return -1;
		}
	}

	public abstract void setFromDecoration(XMLElement fromDecoration);

	public abstract void setToDecoration(XMLElement toDecoration);

	public void setFromLabel(XMLElement fromLabel) {
	    replaceProperty("fromLabel", fromLabel);
	}

	public void setToLabel(XMLElement toLabel) {
	    replaceProperty("toLabel", toLabel);
	}
	
	public Direction getDrawDirection() {
		String dd = getAttribute("drawDirection");
		if (dd.length() == 0) {
			return null;
		}
		return Direction.valueOf(dd);
	}
	
	public XMLElement getFrom() {
		Element fromEl = getProperty("from");
		String reference = fromEl.getAttribute("reference");
		XMLElement from = (XMLElement) ownerDocument.getChildElementById(ownerDocument, reference);
		return from;
	}

	public XMLElement getTo() {
		Element toEl = getProperty("to");
		String reference = toEl.getAttribute("reference");
		XMLElement to = (XMLElement) ownerDocument.getChildElementById(ownerDocument, reference);
		return to;
	}

	public void setFrom(XMLElement v) {
		XMLElement from = (XMLElement) ownerDocument.createElement("from");
		from.setAttribute("reference", v.getID());
		replaceProperty("from", from);
		from = v;
	}

	public void setTo(XMLElement v) {
		XMLElement to = (XMLElement) ownerDocument.createElement("to");
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