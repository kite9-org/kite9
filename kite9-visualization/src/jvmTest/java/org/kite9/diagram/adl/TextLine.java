package org.kite9.diagram.adl;

import org.kite9.diagram.model.DiagramElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: rename to text-box.  This is a formatted area containing text, and could
 * consist of several lines of text.
 * 
 * @author robmoffat
 *
 */
public class TextLine extends AbstractMutableXMLElement {

	private static final long serialVersionUID = -1917135065467101779L;
	

	List<Symbol> symbols = new ArrayList<Symbol>();

	public TextLine() {
		this.tagName = "text-line";
	}
	
	public TextLine(String text, Document doc) {
		this(createID(), "text-line", text, doc);
	}

	public TextLine(String id, String text) {
		this(id, "text-line", text, TESTING_DOCUMENT);
	}
	
	public TextLine(String text) {
		this(text, TESTING_DOCUMENT);
	}
	
	public TextLine(String id, String tag, String text, Document doc) {
		super(id, tag, doc);
		setText(text);
	}
	
	public TextLine(String id, String tag, String text) {
		this(id, tag, text, TESTING_DOCUMENT);
	}

	public Element getSymbols() {
		return getProperty("symbols");
	}
	
	public void setSymbols(Element syms) {
		replaceProperty("symbols", syms);
	}
	
	public String toString() {
		return "[TL:"+getText()+"]";
	}

	@Override
	protected Node newNode() {
		return new TextLine();
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
