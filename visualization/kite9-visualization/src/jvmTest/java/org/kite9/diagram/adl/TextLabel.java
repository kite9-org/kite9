package org.kite9.diagram.adl;

import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;
import org.w3c.dom.Document;

public class TextLabel extends TextLine {

	public TextLabel(String text, Document doc) {
		super(AbstractMutableXMLElement.createID(), "label", text, doc);
	}

	public TextLabel(String text) {
		this(text, TESTING_DOCUMENT);
	}

	public TextLabel(String text, String style) {
		this(text, TESTING_DOCUMENT);
		if (style.length() > 0) {
			setAttribute("style", style);
		}
	}

	public TextLabel(String text, Direction lp) {
		this(text, formatPlacement(lp));
	}

	public static String formatPlacement(Direction d) {
		if (d != null) {
			return "--kite9-direction: " + d.toString().toLowerCase() + "; ";
		} else {
			return "";
		}
	}

	public static String formatHoriz(HorizontalAlignment d) {
		if (d != null) {
			return "--kite9-horizontal-align: " + d.toString().toLowerCase() + "; ";
		} else {
			return "";
		}
	}

	public static String formatVert(VerticalAlignment d) {
		if (d != null) {
			return "--kite9-horizontal-align: " + d.toString().toLowerCase() + "; ";
		} else {
			return "";
		}
	}

	public TextLabel(String text, Direction lp, HorizontalAlignment ha) {
		this(text, formatPlacement(lp) + formatHoriz(ha));
	}

	public TextLabel(String text, Direction lp, VerticalAlignment ha) {
		this(text, formatPlacement(lp) + formatVert(ha));
	}
}