package org.kite9.framework.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.anim.dom.SVGGraphicsElement;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.StyleDeclarationProvider;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.ParsedURL;
import org.kite9.diagram.batik.HasSVGGraphics;
import org.kite9.diagram.batik.templater.XMLProcessor;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.DiagramElementFactory;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.dom.XMLHelper;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
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
		
		if ((id == null) || (id.length()==0)) {
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
	
		((Kite9XMLElement)e).setTagName(propertyName);
		((Kite9XMLElement)e).setOwnerDocument((ADLDocument) this.ownerDocument); 
		
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

	public Iterator<Kite9XMLElement> iterator() {
		final List<Kite9XMLElement> elems = new ArrayList<Kite9XMLElement>();
		
		new XMLProcessor() {

			@Override
			public void processContents(Node from) {
				if (from instanceof Element) {
					NodeList nl = from.getChildNodes();
					for (int i = 0; i < nl.getLength(); i++) {
						Node item = nl.item(i);
						if (item instanceof Kite9XMLElement) {
							elems.add((Kite9XMLElement) item);
						} else {
							processContents(item);
						}
					}
				}
			}
		}.processContents(this);
		
		return elems.iterator();
	}
	
	public int getChildXMLElementCount() {
		int out = 0;
		NodeList childNodes2 = getChildNodes();
		for (int i = 0; i < childNodes2.getLength(); i++) {
			Node n = childNodes2.item(i);
			if (n instanceof Kite9XMLElement) {
				out++;
			}
		}
		
		return out;
	}
	
	protected DiagramElement cachedDiagramElement;
	
	public DiagramElement getDiagramElement() {
		
		if (cachedDiagramElement == null) {
			DiagramElementFactory f = getOwnerDocument().getImplementation().getDiagramElementFactory();
			
			if (f == null) {
				throw new Kite9ProcessingException("No configured DiagramElementFactory on DOMImplementation");
			}
			
			cachedDiagramElement = f.createDiagramElement(this, getParentElement());
		}
		
		return cachedDiagramElement;
	}
	
	private Kite9XMLElement getParentKite9Element() {
		Node n = getParentNode();
		do {
			if (n instanceof Kite9XMLElement) {
				return (Kite9XMLElement) n;
			} else if (n == null) {
				return null;
			} else {
				n = n.getParentNode();
			}
		} while (true);
	}
	
	protected DiagramElement getParentElement() {
		Kite9XMLElement p = getParentKite9Element();
		return (p == null) ? null : p.getDiagramElement();
	}
	
	public StyleMap getComputedStyleMap(String pseudoElement) {
		return sm;
	}

	public void setComputedStyleMap(String pseudoElement, StyleMap sm) {
		this.sm = sm;
	}

	public StyleDeclarationProvider getOverrideStyleDeclarationProvider() {
		return null;
	}


	public boolean isPseudoInstanceOf(String pseudoClass) {
		return false;
	}

	public Value getCSSStyleProperty(String name) {
		CSSEngine e = getOwnerDocument().getCSSEngine();
		int pi = e.getPropertyIndex(name);
		return e.getComputedStyle(this, null, pi);
	}

	@Override
	public Element output(Document d) {
		DiagramElement de = getDiagramElement();
		if (de instanceof HasSVGGraphics) {
			return ((HasSVGGraphics) de).output(d);
		} else {
			return null;
		}
	}
	
}