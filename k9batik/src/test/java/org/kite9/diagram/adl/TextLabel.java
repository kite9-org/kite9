package org.kite9.diagram.adl;

import org.kite9.diagram.model.style.LabelPlacement;
import org.w3c.dom.Document;

public class TextLabel extends TextLine {

	public TextLabel(String text, Document doc) {
		super(AbstractMutableXMLElement.createID(), "label", text, doc);
	}

	public TextLabel(String text) {
		this(text, TESTING_DOCUMENT);
	}
	
	public TextLabel(String text, LabelPlacement lp) {
		this(text, TESTING_DOCUMENT);
		setAttribute("style", "--kite9-label-placement: "+lp.toString().toLowerCase().replace("_","-"));
	}
}
