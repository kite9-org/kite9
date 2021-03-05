package org.kite9.diagram.adl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class Key extends AbstractMutableXMLElement {

	private static final long serialVersionUID = 7705875104684442878L;
	
	public Element getBodyText() {
		return getProperty("bodyText");
	}

	public void setBodyText(Element bodyText) {
		replaceProperty("bodyText", bodyText);
	}
	
	public Element getBoldText() {
		return getProperty("boldText");
	}

	public void setBoldText(Element boldText) {
		replaceProperty("boldText", boldText);
	}
	
	public Element convert(List<Symbol> symbols) {
		Element out = (Element) ownerDocument.createElement("text-lines");
		if (symbols == null) {
			return out;
		}

		for (Symbol s : symbols) {
			List<Symbol> sl = new ArrayList<Symbol>(1);
			sl.add(s);
			out.appendChild(new TextLine(s.getText()));
		}
		return out;
	}
	
	public Key() {		
		this.tagName = "key";
	}
	

	public Key(String boldText, String bodyText, List<Symbol> symbols) {
		this(null, boldText, bodyText, symbols, TESTING_DOCUMENT);
	}

	
	public Key(String id, String boldText, String bodyText, List<Symbol> symbols, Document doc) {
		super(id, "key", doc);
		
		if (boldText != null) {
			setBoldText(new TextLine(boldText));
		}

		if (bodyText != null) {
			setBodyText(new TextLine(bodyText));
		}

		if (symbols != null) {
			setSymbols(convert(symbols));
		}
	}
	
	public Element getSymbols() {
		return getProperty("text-lines");
	}

	public void setSymbols(Element symbols) {
		replaceProperty("text-lines", symbols);
	}
	
	public String toString() {
		return "KEY";
	}

	@Override
	protected Node newNode() {
		return new Key();
	}

}
