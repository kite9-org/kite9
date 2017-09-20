package org.kite9.diagram.adl;

import org.kite9.framework.xml.ADLDocument;

public class LabelTextLine extends TextLine {

	public LabelTextLine(String text, ADLDocument doc) {
		super(createID(), "text-label", text, doc);
	}

	public LabelTextLine(String text) {
		this(text, TESTING_DOCUMENT);
	}
}
