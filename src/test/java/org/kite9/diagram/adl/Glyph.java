package org.kite9.diagram.adl;

import java.util.List;

import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.AbstractStyleableXMLElement;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.w3c.dom.Node;


/**
 * A Glyph is a white node on the diagram which has a fixed hierarchical position
 * within its container.  It has a label and optionally a type, and it can optionally
 * contain multiple rows of text.
 * 
 * @author robmoffat
 *
 */
public class Glyph extends AbstractStyleableXMLElement {

	private static final long serialVersionUID = -6572545083931316651L;
	
	public Glyph() {
		this.tagName = "glyph";
	}
	
	public Glyph(String id, ADLDocument doc) {
		super(id, "glyph", doc);
	}
	
	public Glyph(String id, String stereotype, String label,  List<Kite9XMLElement> text, List<Kite9XMLElement> symbols, boolean divider, ADLDocument doc) {
		super(id, "glyph", doc);
		
		if ((stereotype != null) && (stereotype.length() > 0)) {
			setStereotype(new TextLine(id+"-stereo", "stereotype", stereotype, doc));
		}
		
		if ((label != null) && (label.length() > 0)) {
			setLabel(new TextLine(id+"-label", "label", label, doc));
		}
		
		if (text!=null) {
			setText(new ContainerProperty(id+"-text-lines", "text-lines", doc, text));
		}
		
		if (symbols!=null) {
			setSymbols(new ContainerProperty(id+"-symbols", "symbols", doc, symbols));
		}		
	}
	
	public Glyph(String stereotype, String label,  List<Kite9XMLElement> text, List<Kite9XMLElement> symbols) {
		this(createID()+(label == null ? "" : "-"+label.toLowerCase()), stereotype, label, text, symbols, false, TESTING_DOCUMENT);
	}

	public Glyph(String id, String stereotype, String label,  List<Kite9XMLElement> text, List<Kite9XMLElement> symbols) {
		this(id, stereotype, label, text, symbols, false, TESTING_DOCUMENT);
	}

	public Kite9XMLElement getStereotype() {
		return getProperty("stereotype");
	}

	public void setStereotype(Kite9XMLElement sterotype) {
		replaceProperty("stereotype", sterotype);
	}

	public Kite9XMLElement getText() {
		return getProperty("text-lines");
	}

	public void setText(Kite9XMLElement text) {
		replaceProperty("text-lines", text);
	}

	public Kite9XMLElement getSymbols() {
		return getProperty("symbols");
	}
	
	public void setSymbols(Kite9XMLElement syms) {
		replaceProperty("symbols", syms);
	}

	public boolean hasDimension() {
		return true;
	}
	
	public Kite9XMLElement getLabel() {
		return getProperty("label");
	}

	public void setLabel(Kite9XMLElement name) {
		replaceProperty("label", name);
	}
	
	public String toString() {
		return "[G:"+getID()+"]";
	}
	
	@Override
	protected Node newNode() {
		return new Glyph(null, (ADLDocument) ownerDocument);
	}
	
}