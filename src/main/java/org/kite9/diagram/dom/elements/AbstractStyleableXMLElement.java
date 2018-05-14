package org.kite9.diagram.dom.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.batik.anim.dom.SVGGraphicsElement;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleDeclarationProvider;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.ParsedURL;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.model.DiagramElementFactory;
import org.kite9.diagram.dom.model.HasSVGRepresentation;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.processors.xpath.XPathAware;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public abstract class AbstractStyleableXMLElement extends SVGGraphicsElement implements StyledKite9SVGElement, XPathAware {
	
	protected String tagName;

	public AbstractStyleableXMLElement(String name, ADLDocument owner) {
		super(name, owner);
		this.tagName = name;
	}
	
	public ParsedURL getCSSBase() {
	    String bu = getBaseURI();
	    return bu == null ? null : new ParsedURL(bu);
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
	

	@Override
	public String getNamespaceURI() {
		return XMLHelper.KITE9_NAMESPACE;
	}

	@Override
	public String getLocalName() {
		return getNodeName();
	}


	public ADLDocument getOwnerDocument() {
		return (ADLDocument) super.getOwnerDocument();
	}

	public final String getID() {
		return getAttribute("id");
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
		Value out = getCSSStyleProperty(this, name);
//		System.out.println("getting style for "+this+" "+name+ " "+ out);
		return out;
	}

	public static Value getCSSStyleProperty(CSSStylableElement el, String name) {
		CSSEngine e = ((ADLDocument) el.getOwnerDocument()).getCSSEngine();
		int pi = e.getPropertyIndex(name);
		return e.getComputedStyle(el, null, pi);
	}

	@Override
	public Element output(Document d) {
		DiagramElement de = getDiagramElement();
		if (de instanceof HasSVGRepresentation) {
			return ((HasSVGRepresentation) de).output(d);
		} else {
			return null;
		}
	}

	@Override
	public Map<String, String> getXPathVariables() {
		if (getDiagramElement() instanceof XPathAware) {
			return ((XPathAware) getDiagramElement()).getXPathVariables();
		}
		
		return Collections.emptyMap();
	}

	
}