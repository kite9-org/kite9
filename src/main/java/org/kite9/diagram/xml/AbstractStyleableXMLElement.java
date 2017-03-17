package org.kite9.diagram.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.anim.dom.SVGGraphicsElement;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.StyleDeclarationProvider;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.ParsedURL;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.style.DiagramElementFactory;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.serialization.ADLExtensibleDOMImplementation;
import org.kite9.framework.serialization.XMLHelper;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public abstract class AbstractStyleableXMLElement extends SVGGraphicsElement implements StyledKite9SVGElement {

	/**
	 * Used only in test methods.
	 */
	public static ADLDocument TESTING_DOCUMENT = new ADLDocument();
	protected String tagName;
	boolean readonly = false;
	private static int counter = 0;

	public AbstractStyleableXMLElement(String name, ADLDocument owner) {
		super(name, owner);
		this.tagName = name;
	}
	
	public AbstractStyleableXMLElement(String id, String tag, ADLDocument doc) {
		this(tag, doc);
		
		if (id == null) {
			id = createID();
		}
		
		setID(id);
	}
	
	public ParsedURL getCSSBase() {
	    String bu = getBaseURI();
	    return bu == null ? null : new ParsedURL(bu);
	}

	protected static synchronized String createID() {
		return AUTO_GENERATED_ID_PREFIX+counter++;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean v) {
		this.readonly = v;
	}

	public String getNodeName() {
		return tagName;
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
					throw new Kite9ProcessingException("Not a unique node name: "+name);
				}
			}
		}
	
		return found;
	}

	public <E extends Element> E replaceProperty(String propertyName, E e) {
		E existing = getProperty(propertyName);
		if (e == null) {
			if (existing != null) {
				this.removeChild(existing);
			}
		 	return null;
		}
	
		((XMLElement)e).setTagName(propertyName);
		((XMLElement)e).setOwnerDocument((ADLDocument) this.ownerDocument); 
		
		if (!e.getNodeName().equals(propertyName)) {
			throw new Kite9ProcessingException("Incorrect name.  Expected "+propertyName+" but was "+e.getNodeName());
		}
		
		if (existing != null) {
			this.removeChild(existing);
		}
		
		this.appendChild(e);
		
		return e;
	}

	public void setTagName(String name) {
		this.tagName = name;
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

	@Override
	public String getNamespaceURI() {
		return XMLHelper.KITE9_NAMESPACE;
	}

	@Override
	public String getLocalName() {
		return getNodeName();
	}

	@Override
	public void setAttribute(String name, String value) throws DOMException {
		if (value == null) {
			removeAttribute(name);
		} else {
			super.setAttribute(name, value);
		}		
	}
//
//	public Object getParent() {
//		return parent;
//	}
//
//	public void setParent(Object parent) {
//		this.parent = parent;
//	}

	public void setOwnerDocument(ADLDocument doc) {
		this.ownerDocument = doc;
	}

	public ADLDocument getOwnerDocument() {
		return (ADLDocument) super.getOwnerDocument();
	}

	public final String getID() {
		return getAttribute("id");
	}

	public void setID(String id) {
		setAttribute("id", id);
	}

	public static final String AUTO_GENERATED_ID_PREFIX = "auto:";

	public static void resetCounter() {
		counter = 0;
	}

	protected StyleMap sm;

	public AbstractStyleableXMLElement() {
		super();
	}

	public String getXMLId() {
		return getId();
	}
	
	public boolean hasContent() {
		return true;
	}

	public Iterator<XMLElement> iterator() {
		NodeList childNodes2 = getChildNodes();
		List<XMLElement> elems = new ArrayList<XMLElement>(childNodes2.getLength());
		for (int i = 0; i < childNodes2.getLength(); i++) {
			Node n = childNodes2.item(i);
			if (n instanceof XMLElement) {
				elems.add((XMLElement) n);
			}
		}
		
		return elems.iterator();
	}
	
	public int getChildXMLElementCount() {
		int out = 0;
		NodeList childNodes2 = getChildNodes();
		for (int i = 0; i < childNodes2.getLength(); i++) {
			Node n = childNodes2.item(i);
			if (n instanceof XMLElement) {
				out++;
			}
		}
		
		return out;
	}
	
	protected DiagramElement cachedDiagramElement;
	
	public DiagramElement getDiagramElement() {
		
		if (cachedDiagramElement == null) {
			DiagramElementFactory f = ((ADLExtensibleDOMImplementation) getOwnerDocument().getImplementation()).getDiagramElementFactory();
			
			if (f == null) {
				throw new Kite9ProcessingException("No configured DiagramElementFactory on DOMImplementation");
			}
			
			cachedDiagramElement = f.createDiagramElement(this, getParentElement());
		}
		
		return cachedDiagramElement;
	}
	
	protected DiagramElement getParentElement() {
		Node n = getParentNode();
		if (n instanceof XMLElement) {
			XMLElement p = (XMLElement) n;
			return (p == null) ? null : p.getDiagramElement();
		} else {
			return null;
		}
	}
	
	
	public String getShapeName() {
		return getAttribute("shape");
	}
	
	public void setShapeName(String s) {
		setAttribute("shape", s);
	}


	public void setClasses(String s) {
		setAttribute("class", s);
	}
	
	public String getClasses() {
		return getAttribute("class");
	}

	public void setStyle(String s) {
		setAttribute("style", s);
	}
	
	public StyleMap getComputedStyleMap(String pseudoElement) {
		return sm;
	}

	public void setComputedStyleMap(String pseudoElement, StyleMap sm) {
		this.sm = sm;
	}

	public String getCSSClass() {
		return getAttribute("class");
	}
	

	public StyleDeclarationProvider getOverrideStyleDeclarationProvider() {
		return null;
	}


	public boolean isPseudoInstanceOf(String pseudoClass) {
		return false;
	}

	public Value getCSSStyleProperty(String name) {
		CSSEngine e = getOwnerDocument().getCSSEngine();
		return e.getComputedStyle(this, null, e.getPropertyIndex(name));
	}

}