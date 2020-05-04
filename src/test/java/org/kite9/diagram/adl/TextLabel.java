package org.kite9.diagram.adl;

import org.kite9.diagram.dom.elements.ADLDocument;

public class TextLabel extends TextLine {

	public TextLabel(String text, ADLDocument doc) {
		super(TESTING_DOCUMENT.createUniqueId(), "label", text, doc);
	}

	public TextLabel(String text) {
		this(text, TESTING_DOCUMENT);
	}
}
