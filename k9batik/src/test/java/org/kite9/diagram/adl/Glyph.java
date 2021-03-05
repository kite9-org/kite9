package org.kite9.diagram.adl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;


/**
 * A Glyph is a white node on the diagram which has a fixed hierarchical position
 * within its container.  It has a label and optionally a type, and it can optionally
 * contain multiple rows of text.
 * 
 * @author robmoffat
 *
 */
public class Glyph extends AbstractMutableXMLElement {

	private static final long serialVersionUID = -6572545083931316651L;
	
	public Glyph() {
		this.tagName = "glyph";
	}
	
	public Glyph(String id, Document doc) {
		super(id, "glyph", doc);
	}
	
	public Glyph(String id, String stereotype, String label, List<Element> text, List<Element> symbols, boolean divider, Document doc) {
		super(id, "glyph", doc);

		if (symbols!=null) {
			setSymbols(new ContainerProperty(id+"-symbols", "symbols", doc, symbols));
		}		
		
		if ((stereotype != null) && (stereotype.length() > 0)) {
			setStereotype(new TextLine(id+"-stereo", "stereotype", stereotype, doc));
		}

		if ((label != null) && (label.length() > 0)) {
			setLabel(new TextLine(id+"-label", "label", label, doc));
		}
		
		if (text!=null) {
			setText(new ContainerProperty(id+"-text-lines", "text-lines", doc, text));
		}
		
	}
	
	public Glyph(String stereotype, String label,  List<Element> text, List<Element> symbols) {
		this(AbstractMutableXMLElement.createID()+(label == null ? "" : "-"+label.toLowerCase()), stereotype, label, text, symbols, false, TESTING_DOCUMENT);
	}

	public Glyph(String id, String stereotype, String label,  List<Element> text, List<Element> symbols) {
		this(id, stereotype, label, text, symbols, false, TESTING_DOCUMENT);
	}

	public Element getStereotype() {
		return getProperty("stereotype");
	}

	public void setStereotype(Element sterotype) {
		replaceProperty("stereotype", sterotype);
	}

	public Element getText() {
		return getProperty("text-lines");
	}

	public void setText(Element text) {
		replaceProperty("text-lines", text);
	}

	public Element getSymbols() {
		return getProperty("symbols");
	}
	
	public void setSymbols(Element syms) {
		replaceProperty("symbols", syms);
	}

	public boolean hasDimension() {
		return true;
	}
	
	public Element getLabel() {
		return getProperty("label");
	}

	public void setLabel(Element name) {
		replaceProperty("label", name);
	}
	
	public String toString() {
		return "[G:"+getID()+"]";
	}
	
	@Override
	protected Node newNode() {
		return new Glyph(null, (Document) ownerDocument);
	}
	
}