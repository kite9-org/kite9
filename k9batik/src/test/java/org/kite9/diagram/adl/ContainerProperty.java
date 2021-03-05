package org.kite9.diagram.adl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Eventually, this will be for all containers.  But for now, it's just a few things.
 * Originally, contents were held in a List.  This is not a list, but hopefully it will suffice
 * being an {@link Iterable}.
 * 
 * @author robmoffat
 *
 */
public class ContainerProperty extends AbstractMutableXMLElement {

	public ContainerProperty(String id, String part, Document d) {
		super(id, part, d);
	}
	
	public ContainerProperty(String id, String part, Document d, Collection<? extends Element> contents) {
		this(id, part, d);
		for (Element e : contents) {
			appendChild(e);
		}
	}

	@Override
	protected Node newNode() {
		return new ContainerProperty(createID(), tagName, (Document) ownerDocument);
	}

	public int size() {
		return getChildElementCount();
	}

	public int compareTo(DiagramElement o) {
		throw new Kite9ProcessingException("not implemented");
	}

	public void clear() {
		while (getChildNodes().getLength() > 0) {
			removeChild(getChildNodes().item(0));
		}
	}
	
	public List<Element> asList() {
		ArrayList<Element> out = new ArrayList<Element>(size());
		for (Element e : iterator(this)) {
			out.add(e);
		}
		return out;
	}
}
