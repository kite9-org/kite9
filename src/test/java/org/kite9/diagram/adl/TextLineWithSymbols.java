package org.kite9.diagram.adl;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.model.DiagramElement;
import org.w3c.dom.Node;

/**
 * TODO: rename to text-box.  This is a formatted area containing text, and could
 * consist of several lines of text.
 * 
 * @author robmoffat
 *
 */
public class TextLineWithSymbols extends AbstractMutableXMLElement {

	private static final long serialVersionUID = -1917135065467101779L;
	

	List<Symbol> symbols = new ArrayList<Symbol>();

	public TextLineWithSymbols() {
		this.tagName = "text-line";
	}
	
	public TextLineWithSymbols(String text, ADLDocument doc) {
		this(null, "symbol-text-line", text, null, doc);
	}
	
	public TextLineWithSymbols(String text) {
		this(text, TESTING_DOCUMENT);
	}
	
	public TextLineWithSymbols(String text, List<Symbol> symbols) {
		this(null, "symbol-text-line", text, symbols, TESTING_DOCUMENT);
	}

	
	public TextLineWithSymbols(String id, String tag, String text, List<Symbol> symbols, ADLDocument doc) {
		super(id, tag, doc);
		replaceProperty("k9-text", new TextLine(id+"-txt", "text", text, doc));
		if (symbols != null) {
			setSymbols(new ContainerProperty(id+"-symbols", "symbols", doc, symbols));
		}
	}

	public Kite9XMLElement getSymbols() {
		return getProperty("symbols");
	}
	
	public void setSymbols(Kite9XMLElement syms) {
		replaceProperty("symbols", syms);
	}
	
	public String toString() {
		return "[TL:"+getText()+"]";
	}

	@Override
	protected Node newNode() {
		return new TextLineWithSymbols();
	}

	public void setText(String text) {
		setTextData(text);
	}

	public String getText() {
		return getTextData();
	}

	public DiagramElement getOwner() {
		Node parent = getParentNode();
		if (parent instanceof ContainerProperty) {
			return (DiagramElement) parent.getParentNode();
		} else {
			return (DiagramElement) parent;
		}
		
	}

}
