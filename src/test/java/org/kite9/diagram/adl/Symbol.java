package org.kite9.diagram.adl;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.AbstractStyleableXMLElement;
import org.w3c.dom.Node;

public class Symbol extends AbstractStyleableXMLElement {

	private static final long serialVersionUID = 3578883565482903409L;
	
	public enum SymbolShape { HEXAGON, CIRCLE,  DIAMOND, SQUARE };

	@Override
	public boolean equals(Object arg0) {
		if (arg0 == this)
			return true;
	
		if (arg0 instanceof Symbol)
			return ((Symbol)arg0).getText().equals(getText());
		else
			return false;
	}

	@Override
	public String toString() {
		return "Symbol: "+getText();
	}
	
	public Symbol() {
		this.tagName = "symbol";
	}

	public Symbol(String text, char preferredChar, SymbolShape shape, ADLDocument doc) {
		super(null, "symbol", doc);
		setTextContent(""+preferredChar);
		setChar(preferredChar);
		setShape(shape);
		setClass(shape.toString().toLowerCase());
	}

	public void setClass(String c) {
		setAttribute("class", c);
	}

	public Symbol(String string, char c, SymbolShape shape) {
		this(string, c, shape, TESTING_DOCUMENT);
	}

	public char getChar() {
		return getAttribute("theChar").charAt(0);
	}

	public void setChar(char theChar) {
		setAttribute("theChar", ""+theChar);
	}

	public SymbolShape getShape() {
		return SymbolShape.valueOf(getAttribute("shape"));
	}

	public void setShape(SymbolShape shape) {
		setAttribute("shape", shape.name());
	}

	public int compareTo(DiagramElement o) {
		if (o instanceof Symbol) {
			int out = ((Character)this.getChar()).compareTo(((Symbol) o).getChar());
			if (out==0) {
				return this.getShape().compareTo(((Symbol) o).getShape());
			} else {
				return out;
			}
		} else {
			return 1;
		}
	}

	@Override
	protected Node newNode() {
		return new Symbol("new", 'n', SymbolShape.CIRCLE, (ADLDocument) ownerDocument);
	}

	public void setText(String text) {
		setTextData(text);
	}

	public String getText() {
		return getTextData();
	}

	public String getXMLId() {
		return getAttribute("id");
	}
}
