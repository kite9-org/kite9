package org.kite9.diagram.adl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;


/**
 * A grid is a container pretty much like a context, but is designed to be
 * used with cells inside.
 * 
 * @author robmoffat
 *
 */
public class Grid extends AbstractXMLContainerElement {
	
	@Override
	public String toString() {
		return "[C:"+getID()+"]";
	}

	private static final long serialVersionUID = -311300007972605832L;

	public Grid() {
		this.tagName = "grid";
	}
	
	public Grid(String id, List<Element> contents, Element label, Document doc) {
		super(id, "grid", doc);
		
		if (contents != null) {
			for (Element contained : contents) {
				if (contained != null) {
					appendChild(contained);
				}
			}
		}
		
		addLabel(label);
	}

	public Grid(String id, List<Element> contents, Element label) {
		this(id, contents, label, TESTING_DOCUMENT);
	}

	public Grid(List<Element> contents, Element label) {
		this(AbstractMutableXMLElement.createID(), contents, label);
	}

	@Override
	protected Node newNode() {
		return new Grid();
	}
}
