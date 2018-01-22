package org.kite9.diagram.batik.templater;

import org.kite9.diagram.batik.templater.ValueReplacingProcessor.ValueReplacer;
import org.w3c.dom.Element;

public class ParentElementValueReplacer implements ValueReplacer {
	
	Element e;
	String text;
	
	public ParentElementValueReplacer(Element parent) {
		super();
		this.e = parent;
		this.text = e.getTextContent();
	}

	/**
	 * Handles replacement of {@someattribute} within the SVG.
	 */
	public String getReplacementValue(String in) {
		Element parent = this.e;
		if (in.startsWith("@")) {
			while (parent != null) {
				if (parent.hasAttribute(in.substring(1))) {
					return parent.getAttribute(in.substring(1));
				} 
				parent=(Element) parent.getParentNode();
			}
		} 
		
		return "{"+in+"}";	// couldn't be replaced - leave original
	}

}