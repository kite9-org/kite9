package org.kite9.framework.xml;

import org.kite9.diagram.model.position.Layout;

public abstract class AbstractXMLContainerElement extends AbstractStyleableXMLElement {

	public AbstractXMLContainerElement() {
		super();
	}

	public AbstractXMLContainerElement(String name, ADLDocument owner) {
		super(name, owner);
	}

	public AbstractXMLContainerElement(String id, String tag, ADLDocument doc) {
		super(id, tag, doc);
	}

	public void setLabel(Kite9XMLElement label) {
	    replaceProperty("label", label);
	}
	
	public Kite9XMLElement getLabel() {
		return getProperty("label");
	}
	
	public void setLayoutDirection(Layout layoutDirection) {
		if (layoutDirection != null) {
			setAttribute("style", "kite9-layout: "+layoutDirection.toString().toLowerCase()+";");
		} else {
			removeAttribute("style");
		}
	}
}
