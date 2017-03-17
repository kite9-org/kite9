package org.kite9.diagram.xml;

import java.util.List;

import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.position.Layout;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Node;


/**
 * This class represents a whole diagram within ADL.  A diagram itself is a container of either Glyphs or
 * contexts.  It also has a key explaining what Symbol's mean.
 * 
 * Arrows and ArrowLinks are implicitly contained in the diagram since they are connected to the Glyphs.
 * 
 * @author robmoffat
 *
 */
public class DiagramXMLElement extends AbstractXMLContainerElement {

	private static final long serialVersionUID = -7727042271665853389L;
	
	public DiagramXMLElement() {
		this(TESTING_DOCUMENT);
	}
	
	public DiagramXMLElement(ADLDocument doc) {
		this(createID(), doc);
	}
	
	public DiagramXMLElement(String id, ADLDocument doc) {
		this(id, null, null, doc);
	}

	public DiagramXMLElement(String id, List<XMLElement> contents, XMLElement k) {
		this(id, contents, k, TESTING_DOCUMENT);
	}
	
	public DiagramXMLElement(String id, List<XMLElement> contents, XMLElement k, ADLDocument doc) {
		super(id, "diagram", doc);
		if (doc.getDocumentElement() == null) {
			doc.appendChild(this);
		} 
		
		if (contents != null) {
			for (XMLElement contained : contents) {
				appendChild(contained);
			}
		}
		if (k != null) {
			replaceProperty("key", k);
		}
		
		// home all temporary connections (due to Link)
		for (XMLElement xmlElement : doc.getConnectionElements()) {
			appendChild(xmlElement);
		}
		
		// set ranks for all children that don't have them
		int rank = 0;
		for (Node childElement : this) {
			if (childElement instanceof XMLElement) {
				XMLElement xmlElement = (XMLElement) childElement;
				String rankString = xmlElement.getAttribute("rank");
				if ("".equals(rankString)) {
					xmlElement.setAttribute("rank", ""+(rank++));
				}
			}
		}
		doc.setDiagramCreated(true);
		doc.getConnectionElements().clear();
		
	}
	
	public DiagramXMLElement(String id, List<XMLElement> contents) {
		this(id, contents, null, TESTING_DOCUMENT);
	}

	public DiagramXMLElement(String id, List<XMLElement> contents, Layout l, XMLElement k) {
		this(id, contents, k, TESTING_DOCUMENT);
		this.setLayoutDirection(l);
	}

	public DiagramXMLElement(List<XMLElement> contents, XMLElement k) {
		this(createID(), contents, k);
	}

	public XMLElement getKey() {
		return getProperty("key");
	}

	public void setKey(XMLElement k) {
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
		return new DiagramXMLElement();
	}

	@Override
	public XMLElement getLabel() {
		return getKey();
	}
	
	public Diagram getDiagramElement() {
		return (Diagram) super.getDiagramElement();
	}
	
}
