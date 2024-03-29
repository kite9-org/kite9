package org.kite9.diagram.adl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class models the black body of the arrow, which will have links 
 * entering and leaving it.
 * 
 * @author moffatr
 *
 */

public class LinkBody extends AbstractMutableXMLElement {

	private static final long serialVersionUID = 5054715961820271315L;

	public LinkBody() {
		this.tagName = "link-body";
	}
		
	public Element getLabel() {
		return getProperty("label");
	}

	public void setLabel(Element name) {
		replaceProperty("label", name);
	}

	
	public LinkBody(String id, String label, Document doc) {
		super(id, "link-body", doc);

		if (label != null) {
			setLabel(new TextLine(null, "label", label, doc));
		}
		
	}
	
	public LinkBody(String id, String label) {
		this(id, label, TESTING_DOCUMENT);
	}

	public LinkBody(String label) {
		this(label, label);
	}

	public boolean hasDimension() {
		return true;
	}

	public String toString() {
		return "[A:"+getID()+"]";
	}
	
	@Override
	protected Node newNode() {
		return new LinkBody();
	}

}
