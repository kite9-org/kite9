package org.kite9.diagram.adl;

import java.util.List;

import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.w3c.dom.Node;


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
	
	public Grid(String id, List<Kite9XMLElement> contents, Kite9XMLElement label, ADLDocument doc) {
		super(id, "grid", doc);
		
		if (contents != null) {
			for (Kite9XMLElement contained : contents) {
				if (contained != null) {
					appendChild(contained);
				}
			}
		}
		
		addLabel(label);
	}

	public Grid(String id, List<Kite9XMLElement> contents, Kite9XMLElement label) {
		this(id, contents, label, TESTING_DOCUMENT);
	}

	public Grid(List<Kite9XMLElement> contents, Kite9XMLElement label) {
		this(TESTING_DOCUMENT.createUniqueId(), contents, label);
	}

	@Override
	protected Node newNode() {
		return new Grid();
	}
}
