package org.kite9.diagram.adl;

import org.kite9.framework.xml.ADLDocument;

public class TextLabel extends TextLine {

	public TextLabel(String text, ADLDocument doc) {
		super(createID(), "text-label", text, doc);
	}

	public TextLabel(String text) {
		this(text, TESTING_DOCUMENT);
	}
}
