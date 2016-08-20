package org.kite9.diagram.adl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.AbstractStyleableXMLElement;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Node;

/**
 * Eventually, this will be for all containers.  But for now, it's just a few things.
 * Originally, contents were held in a List.  This is not a list, but hopefully it will suffice
 * being an {@link Iterable}.
 * 
 * @author robmoffat
 *
 */
public class ContainerProperty extends AbstractStyleableXMLElement {

	public ContainerProperty(String part, ADLDocument d) {
		super(part, d);
	}
	
	public ContainerProperty(String part, ADLDocument d, Collection<? extends XMLElement> contents) {
		this(part, d);
		for (XMLElement e : contents) {
			appendChild(e);
		}
	}

	@Override
	protected Node newNode() {
		return new ContainerProperty(tagName, (ADLDocument) ownerDocument);
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
	
	public List<XMLElement> asList() {
		ArrayList<XMLElement> out = new ArrayList<XMLElement>(size());
		for (XMLElement e : this) {
			out.add(e);
		}
		return out;
	}
}
