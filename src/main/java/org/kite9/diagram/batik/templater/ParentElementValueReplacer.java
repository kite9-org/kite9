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
	public String getReplacementValue(String prefix, String attr) {
		Element parent = this.e;
		if ("@".equals(prefix)) {
			while (parent != null) {
				if (parent.hasAttribute(attr)) {
					return parent.getAttribute(attr);
				} 
				parent=(Element) parent.getParentNode();
			}
		} 
		
		return "{"+prefix+attr+"}";	// couldn't be replaced - leave original
	}

}