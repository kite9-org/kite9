package org.kite9.diagram.adl;

import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.GenericDocument;
import org.apache.batik.dom.GenericElement;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMutableXMLElement extends GenericElement {

    public static String TRANSFORM;

    public static DOMImplementation DOM_IMPLEMENTATION;

	public static GenericDocument newDocument() {
		return new GenericDocument(null, DOM_IMPLEMENTATION);
	}

	public static GenericDocument TESTING_DOCUMENT;

	public static List<Element> CONNECTION_ELEMENTS = new ArrayList<>();

	public static int nextId = 100;

	protected String tagName;

	public String getNodeName() {
		return tagName;
	}

	@Override
	public String getLocalName() {
		return getNodeName();
	}

	public String getXMLId() {
		return getId();
	}

	public final String getID() {
		return getAttribute("id");
	}

	public static String getID(Element e) {
		return e.getAttribute("id");
	}

	public AbstractMutableXMLElement(String id, String tag, Document doc) {
		this(tag, doc);
		this.tagName = tag;
		
		if ((id == null) || (id.length()==0)) {
			id = createID();
		}
		
		setID(id);
	}
	

	public static synchronized String createID() {
		return ""+(nextId++);
	}
	

	public void setID(String id) {
		setAttribute("id", id);
	}

	public static final String AUTO_GENERATED_ID_PREFIX = "auto:";

	public AbstractMutableXMLElement() {
		super();
	}

	public AbstractMutableXMLElement(String name, Document owner) {
		super( name, (AbstractDocument) owner);
	}

	@Override
	public void setAttribute(String name, String value) throws DOMException {
		if (value == null) {
			removeAttribute(name);
		} else {
			super.setAttribute(name, value);
		}		
	}

	public void setOwnerDocument(Document doc) {
		this.ownerDocument = (AbstractDocument) doc;
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
		((AbstractMutableXMLElement)e).setOwnerDocument((Document) this.ownerDocument);
		
		if (!e.getNodeName().equals(propertyName)) {
			throw new Kite9ProcessingException("Incorrect name.  Expected "+propertyName+" but was "+e.getNodeName());
		}
		
		if (existing != null) {
			this.removeChild(existing);
		}
		
		this.appendChild(e);
		
		return e;
	}


	@SuppressWarnings("unchecked")
	public <E extends Element> E getProperty(String name) {
		E found = null;
		for (int i = 0; i < getChildNodes().getLength(); i++) {
			Node n = getChildNodes().item(i);
			if ((n instanceof Element) && (((Element)n).getTagName().equals(name))) {
				if (found == null) {
					found = (E) n;
				} else {
					throw new Kite9XMLProcessingException("Not a unique node name: "+name, this);
				}
			}
		}

		return found;
	}

	protected String getTextData() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < getChildNodes().getLength(); i++) {
			Node n = getChildNodes().item(i);
			if (n instanceof Text) {
				sb.append(((Text) n).getData());
			}
		}

		return sb.toString().trim();
	}

	public static List<Element> iterator(Element from) {
		final List<Element> elems = new ArrayList<Element>();

		NodeList nl = from.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node item = nl.item(i);
			if (item instanceof Element) {
				elems.add((Element) item);
			}
		}

		return elems;
	}

}
