package org.kite9.diagram.adl;

import org.kite9.diagram.model.position.Layout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;


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
	
	public Context(String id, List<Element> contents, boolean bordered, Element label, Layout layoutDirection, Document doc) {
		super(id, "context", doc);
		
		if (contents != null) {
			for (Element contained : contents) {
				if (contained != null) {
					appendChild(contained);
				}
			}
		}
		
		setLayoutDirection(layoutDirection);
		
		addLabel(label);
		setBordered(bordered);
	}

	public Context(String id, List<Element> contents, boolean bordered, Element label, Layout layoutDirection) {
		this(id, contents, bordered, label, layoutDirection, TESTING_DOCUMENT);
	}

	public Context(List<Element> contents, boolean b, Element label, Layout l) {
		this(AbstractMutableXMLElement.createID(), contents, b, label, l);
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
