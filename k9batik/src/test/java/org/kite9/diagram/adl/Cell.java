package org.kite9.diagram.adl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;


/**
 * A cell is a container pretty much like a context, but is designed to be
 * used with grid.
 * 
 * @author robmoffat
 *
 */
public class Cell extends AbstractXMLContainerElement {
	
	@Override
	public String toString() {
		return "[C:"+getID()+"]";
	}

	private static final long serialVersionUID = -311300007972605832L;

	public Cell() {
		this.tagName = "cell";
	}
	
	public Cell(String id, List<Element> contents, Document doc) {
		super(id, "cell", doc);
		
		if (contents != null) {
			for (Element contained : contents) {
				if (contained != null) {
					appendChild(contained);
				}
			}
		}

	}

	public Cell(String id, List<Element> contents) {
		this(id, contents, TESTING_DOCUMENT);
	}

	@Override
	protected Node newNode() {
		return new Cell();
	}
}
