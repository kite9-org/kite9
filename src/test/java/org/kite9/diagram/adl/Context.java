package org.kite9.diagram.adl;

import java.util.List;

import org.kite9.diagram.position.Layout;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.AbstractXMLContainerElement;
import org.kite9.diagram.xml.XMLElement;
import org.w3c.dom.Node;


/**
 * A context is a portion of the diagram with a border around it, and a label.  
 * It contains other Glyphs or context to give the diagram a hierarchy.
 * 
 * @author robmoffat
 *
 */
public class Context extends AbstractXMLContainerElement {
	
	@Override
	public String toString() {
		return "[C:"+getID()+"]";
	}

	private static final long serialVersionUID = -311300007972605832L;

	public Context() {
		this.tagName = "context";
	}
	
	public Context(String id, List<XMLElement> contents, boolean bordered, XMLElement label, Layout layoutDirection, ADLDocument doc) {
		super(id, "context", doc);
		
		if (contents != null) {
			for (XMLElement contained : contents) {
				appendChild(contained);
			}
		}
		
		setLayoutDirection(layoutDirection);
		setLabel(label);
		setBordered(bordered);
	}

	public Context(String id, List<XMLElement> contents, boolean bordered, XMLElement label, Layout layoutDirection) {
		this(id, contents, bordered, label, layoutDirection, TESTING_DOCUMENT);
	}

	public Context(List<XMLElement> contents, boolean b, XMLElement label, Layout l) {
		this(createID(), contents, b, label, l);
	}

	public boolean isBordered() {
		return !"false".equals(getAttribute("bordered"));
	}

	public void setBordered(boolean bordered) {
		setAttribute("bordered", ""+bordered);
	}

	@Override
	protected Node newNode() {
		return new Context();
	}
}
