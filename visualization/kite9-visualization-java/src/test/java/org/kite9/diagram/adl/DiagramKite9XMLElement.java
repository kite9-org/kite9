package org.kite9.diagram.adl;

import org.kite9.diagram.model.position.Layout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;


/**
 * This class represents a whole diagram within ADL.  A diagram itself is a container of either Glyphs or
 * contexts.  It also has a key explaining what Symbol's mean.
 * 
 * Arrows and ArrowLinks are implicitly contained in the diagram since they are connected to the Glyphs.
 * 
 * @author robmoffat
 *
 */
public class DiagramKite9XMLElement extends AbstractXMLContainerElement {

	private static final long serialVersionUID = -7727042271665853389L;
	
	public DiagramKite9XMLElement() {
		this(TESTING_DOCUMENT);
	}
	
	public DiagramKite9XMLElement(Document doc) {
		this(AbstractMutableXMLElement.createID(), doc);
	}
	
	public DiagramKite9XMLElement(String id, Document doc) {
		this(id, null, null, doc);
	}

	public DiagramKite9XMLElement(String id, List<Element> contents, Element k) {
		this(id, contents, k, TESTING_DOCUMENT);
	}
	
	public DiagramKite9XMLElement(String id, List<Element> contents, Element k, Document doc) {
		super(id, "diagram", doc);
		if (doc.getDocumentElement() == null) {
			doc.appendChild(this);
		} 
		
		if (contents != null) {
			for (Element contained : contents) {
				appendChild(contained);
			}
		}
		if (k != null) {
			replaceProperty("key", k);
		}
		
		// home all temporary connections (due to Link)
		for (Element xmlElement : AbstractMutableXMLElement.CONNECTION_ELEMENTS) {
			this.ownerDocument.adoptNode(xmlElement);
			appendChild(xmlElement);
		}

		AbstractMutableXMLElement.CONNECTION_ELEMENTS.clear();
		
		// set ranks for all children that don't have them
		int rank = 0;
		for (Node childElement : iterator(this)) {
			if (childElement instanceof Element) {
				Element xmlElement = (Element) childElement;
				String rankString = xmlElement.getAttribute("rank");
				if ("".equals(rankString)) {
					xmlElement.setAttribute("rank", ""+(rank++));
				}
			}
		}

		setAttribute("template", AbstractMutableXMLElement.TRANSFORM);
	}
	
	public DiagramKite9XMLElement(String id, List<Element> contents) {
		this(id, contents, null, TESTING_DOCUMENT);
	}

	public DiagramKite9XMLElement(String id, List<Element> contents, Layout l, Element k) {
		this(id, contents, k, TESTING_DOCUMENT);
		this.setLayoutDirection(l);
	}

	public DiagramKite9XMLElement(List<Element> contents, Element k) {
		this(AbstractMutableXMLElement.createID(), contents, k);
	}

	public Element getKey() {
		return getProperty("key");
	}

	public void setKey(Element k) {
	    replaceProperty("key", k);
	}

	public boolean isBordered() {
		return true;
	}

	public String getName() {
		return getAttribute("name");
	}
	
	public void setName(String name) {
		setAttribute("name", name);
	}

	public String getNodeName() {
		return "diagram";
	}

	@Override
	protected Node newNode() {
		return new DiagramKite9XMLElement();
	}
}
