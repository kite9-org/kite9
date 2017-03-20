package org.kite9.framework.xml;

import java.util.List;

import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.position.Layout;
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
public class DiagramKite9XMLElement extends AbstractXMLContainerElement {

	private static final long serialVersionUID = -7727042271665853389L;
	
	public DiagramKite9XMLElement() {
		this(TESTING_DOCUMENT);
	}
	
	public DiagramKite9XMLElement(ADLDocument doc) {
		this(createID(), doc);
	}
	
	public DiagramKite9XMLElement(String id, ADLDocument doc) {
		this(id, null, null, doc);
	}

	public DiagramKite9XMLElement(String id, List<Kite9XMLElement> contents, Kite9XMLElement k) {
		this(id, contents, k, TESTING_DOCUMENT);
	}
	
	public DiagramKite9XMLElement(String id, List<Kite9XMLElement> contents, Kite9XMLElement k, ADLDocument doc) {
		super(id, "diagram", doc);
		if (doc.getDocumentElement() == null) {
			doc.appendChild(this);
		} 
		
		if (contents != null) {
			for (Kite9XMLElement contained : contents) {
				appendChild(contained);
			}
		}
		if (k != null) {
			replaceProperty("key", k);
		}
		
		// home all temporary connections (due to Link)
		for (Kite9XMLElement xmlElement : doc.getConnectionElements()) {
			appendChild(xmlElement);
		}
		
		// set ranks for all children that don't have them
		int rank = 0;
		for (Node childElement : this) {
			if (childElement instanceof Kite9XMLElement) {
				Kite9XMLElement xmlElement = (Kite9XMLElement) childElement;
				String rankString = xmlElement.getAttribute("rank");
				if ("".equals(rankString)) {
					xmlElement.setAttribute("rank", ""+(rank++));
				}
			}
		}
		doc.setDiagramCreated(true);
		doc.getConnectionElements().clear();
		
	}
	
	public DiagramKite9XMLElement(String id, List<Kite9XMLElement> contents) {
		this(id, contents, null, TESTING_DOCUMENT);
	}

	public DiagramKite9XMLElement(String id, List<Kite9XMLElement> contents, Layout l, Kite9XMLElement k) {
		this(id, contents, k, TESTING_DOCUMENT);
		this.setLayoutDirection(l);
	}

	public DiagramKite9XMLElement(List<Kite9XMLElement> contents, Kite9XMLElement k) {
		this(createID(), contents, k);
	}

	public Kite9XMLElement getKey() {
		return getProperty("key");
	}

	public void setKey(Kite9XMLElement k) {
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

	@Override
	public Kite9XMLElement getLabel() {
		return getKey();
	}
	
	public Diagram getDiagramElement() {
		return (Diagram) super.getDiagramElement();
	}
	
}
