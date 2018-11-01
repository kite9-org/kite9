package org.kite9.diagram.adl;

import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.AbstractStyleableXMLElement;
import org.kite9.diagram.dom.elements.AbstractXPathAwareXMLElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public abstract class AbstractMutableXMLElement extends AbstractXPathAwareXMLElement {

	/**
	 * Used only in test methods.
	 */
	public static ADLDocument TESTING_DOCUMENT = new ADLDocument();
	

	private static int counter = 0;

	public AbstractMutableXMLElement(String id, String tag, ADLDocument doc) {
		this(tag, doc);
		
		if ((id == null) || (id.length()==0)) {
			id = createID();
		}
		
		setID(id);
	}
	

	protected static synchronized String createID() {
		return AUTO_GENERATED_ID_PREFIX+counter++;
	}
	

	public void setID(String id) {
		setAttribute("id", id);
	}

	public static final String AUTO_GENERATED_ID_PREFIX = "auto:";

	public static void resetCounter() {
		counter = 0;
	}

	public AbstractMutableXMLElement() {
		super();
	}

	public AbstractMutableXMLElement(String name, ADLDocument owner) {
		super(name, owner);
	}

	@Override
	public void setAttribute(String name, String value) throws DOMException {
		if (value == null) {
			removeAttribute(name);
		} else {
			super.setAttribute(name, value);
		}		
	}

	public void setOwnerDocument(ADLDocument doc) {
		this.ownerDocument = doc;
	}
	

	public void setTagName(String name) {
		this.tagName = name;
	}


	protected void setTextData(String text) {
		int i = 0;
		while (i < getChildNodes().getLength()) {
			Node child = getChildNodes().item(i);
			if (child instanceof Text) {
				removeChild(child);
			} else {
				i++;
			}
		}
		
		appendChild(ownerDocument.createTextNode(text));
	}


	public <E extends Element> E replaceProperty(String propertyName, E e) {
		E existing = getProperty(propertyName);
		if (e == null) {
			if (existing != null) {
				this.removeChild(existing);
			}
		 	return null;
		}
	
		((AbstractMutableXMLElement)e).setTagName(propertyName);
		((AbstractMutableXMLElement)e).setOwnerDocument((ADLDocument) this.ownerDocument); 
		
		if (!e.getNodeName().equals(propertyName)) {
			throw new Kite9ProcessingException("Incorrect name.  Expected "+propertyName+" but was "+e.getNodeName());
		}
		
		if (existing != null) {
			this.removeChild(existing);
		}
		
		this.appendChild(e);
		
		return e;
	}

}
