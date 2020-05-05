package org.kite9.diagram.adl;

import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.model.style.LabelPlacement;

public class TextLabel extends TextLine {

	public TextLabel(String text, ADLDocument doc) {
		super(TESTING_DOCUMENT.createUniqueId(), "label", text, doc);
	}

	public TextLabel(String text) {
		this(text, TESTING_DOCUMENT);
	}
	
	public TextLabel(String text, LabelPlacement lp) {
		this(text, TESTING_DOCUMENT);
		setAttribute("style", "kite9-label-placement: "+lp.toString().toLowerCase());
	}
}
