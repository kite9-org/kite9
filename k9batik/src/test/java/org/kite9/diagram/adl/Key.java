package org.kite9.diagram.adl;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.w3c.dom.Node;

public class Key extends AbstractMutableXMLElement {

	private static final long serialVersionUID = 7705875104684442878L;
	
	public Kite9XMLElement getBodyText() {
		return getProperty("bodyText");
	}

	public void setBodyText(Kite9XMLElement bodyText) {
		replaceProperty("bodyText", bodyText);
	}
	
	public Kite9XMLElement getBoldText() {
		return getProperty("boldText");
	}

	public void setBoldText(Kite9XMLElement boldText) {
		replaceProperty("boldText", boldText);
	}
	
	public Kite9XMLElement convert(List<Symbol> symbols) {
		Kite9XMLElement out = (Kite9XMLElement) ownerDocument.createElement("text-lines");
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

	
	public Key(String id, String boldText, String bodyText, List<Symbol> symbols, ADLDocument doc) {
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
	
	public Kite9XMLElement getSymbols() {
		return getProperty("text-lines");
	}

	public void setSymbols(Kite9XMLElement symbols) {
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
