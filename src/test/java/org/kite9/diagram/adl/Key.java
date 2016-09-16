package org.kite9.diagram.adl;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.AbstractStyleableXMLElement;
import org.kite9.diagram.xml.GenericXMLElement;
import org.kite9.diagram.xml.XMLElement;
import org.w3c.dom.Node;

public class Key extends AbstractStyleableXMLElement {

	private static final long serialVersionUID = 7705875104684442878L;
	
	public XMLElement getBodyText() {
		return getProperty("bodyText");
	}

	public void setBodyText(XMLElement bodyText) {
		replaceProperty("bodyText", bodyText);
	}
	
	public XMLElement getBoldText() {
		return getProperty("boldText");
	}

	public void setBoldText(XMLElement boldText) {
		replaceProperty("boldText", boldText);
	}
	
	public GenericXMLElement convert(List<Symbol> symbols) {
		GenericXMLElement out = (GenericXMLElement) ownerDocument.createElement("text-lines");
		if (symbols == null) {
			return out;
		}

		for (Symbol s : symbols) {
			List<Symbol> sl = new ArrayList<Symbol>(1);
			sl.add(s);
			out.appendChild(new TextLine(null, "text-line", s.getText(), sl, (ADLDocument) ownerDocument));
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
			setBoldText(new TextLine(null, "boldText", boldText, null, doc));
		}

		if (bodyText != null) {
			setBodyText(new TextLine(null, "bodyText", bodyText, null, doc));
		}

		if (symbols != null) {
			setSymbols(convert(symbols));
		}
	}
	
	public XMLElement getSymbols() {
		return getProperty("text-lines");
	}

	public void setSymbols(XMLElement symbols) {
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
