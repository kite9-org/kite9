package org.kite9.diagram.adl;

import java.util.List;

import org.kite9.diagram.model.position.Layout;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.AbstractXMLContainerElement;
import org.kite9.framework.xml.Kite9XMLElement;
import org.w3c.dom.Node;


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
	
	public Cell(String id, List<Kite9XMLElement> contents, Kite9XMLElement label, Layout layoutDirection, ADLDocument doc) {
		super(id, "cell", doc);
		
		if (contents != null) {
			for (Kite9XMLElement contained : contents) {
				if (contained != null) {
					appendChild(contained);
				}
			}
		}
		
		setLayoutDirection(layoutDirection);
		setLabel(label);
	}

	public Cell(String id, List<Kite9XMLElement> contents, Kite9XMLElement label, Layout layoutDirection) {
		this(id, contents, label, layoutDirection, TESTING_DOCUMENT);
	}

	public Cell(List<Kite9XMLElement> contents, Kite9XMLElement label, Layout l) {
		this(createID(), contents, label, l);
	}

	@Override
	protected Node newNode() {
		return new Cell();
	}
}
