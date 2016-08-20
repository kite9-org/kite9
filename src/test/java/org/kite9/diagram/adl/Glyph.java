package org.kite9.diagram.adl;

import java.util.List;

import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.AbstractStyleableXMLElement;
import org.kite9.diagram.xml.XMLElement;
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
		
	public XMLElement getLabel() {
		return getProperty("label");
	}

	public void setLabel(XMLElement name) {
		replaceProperty("label", name);
	}
	
	public Glyph() {
		this.tagName = "glyph";
	}
	
	public Glyph(String id, ADLDocument doc) {
		super(id, "glyph", doc);
	}
	
	public Glyph(String id, String stereotype, String label,  List<XMLElement> text, List<XMLElement> symbols, boolean divider, ADLDocument doc) {
		super(id, "glyph", doc);
		
		if (stereotype != null) {
			setStereotype(new TextLine(null, "stereotype", stereotype, null, doc));
		}
		
		if (label != null) {
			setLabel(new TextLine(null, "label", label, null, doc));
		}
		
		if (text!=null) {
			setText(new ContainerProperty("text-lines", doc, text));
		}
		
		if (symbols!=null) {
			setSymbols(new ContainerProperty("symbols", doc, symbols));
		}		
	}
	
	public Glyph(String stereotype, String label,  List<XMLElement> text, List<XMLElement> symbols) {
		this(createID(), stereotype, label, text, symbols, false, TESTING_DOCUMENT);
	}

	public Glyph(String id, String stereotype, String label,  List<XMLElement> text, List<XMLElement> symbols) {
		this(id, stereotype, label, text, symbols, false, TESTING_DOCUMENT);
	}

	public XMLElement getStereotype() {
		return getProperty("stereotype");
	}

	public void setStereotype(XMLElement sterotype) {
		replaceProperty("stereotype", sterotype);
	}

	public XMLElement getText() {
		return getProperty("text-lines");
	}

	public void setText(XMLElement text) {
		replaceProperty("text-lines", text);
	}

	public XMLElement getSymbols() {
		return getProperty("symbols");
	}
	
	public void setSymbols(XMLElement syms) {
		replaceProperty("symbols", syms);
	}

	public boolean hasDimension() {
		return true;
	}
	
	
	public String toString() {
		return "[G:"+getID()+"]";
	}
	
	@Override
	protected Node newNode() {
		return new Glyph(null, (ADLDocument) ownerDocument);
	}
	
}