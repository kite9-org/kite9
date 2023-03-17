package org.kite9.diagram.adl;

import org.kite9.diagram.model.position.Layout;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractXMLContainerElement extends AbstractMutableXMLElement {

	public AbstractXMLContainerElement() {
		super();
	}

	public AbstractXMLContainerElement(String name, Document owner) {
		super(name, owner);
	}

	public AbstractXMLContainerElement(String id, String tag, Document doc) {
		super(id, tag, doc);
	}
	

	public void addLabel(Element label) {
		if (label != null) {
			appendChild(label);
		}
	}
	
	public void setLayoutDirection(Layout layoutDirection) {
		if (layoutDirection != null) {
			setAttribute("style", "--kite9-layout: "+layoutDirection.toString().toLowerCase()+";");
		} else {
			removeAttribute("style");
		}
	}
}
