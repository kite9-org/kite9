package org.kite9.framework.xml;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.util.XMLConstants;
import org.kite9.framework.dom.ADLExtensibleDOMImplementation;
import org.kite9.framework.dom.XMLHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.stylesheets.StyleSheetList;

/**
 * Now, Kite9 elements are first-class members of SVG, and support SVG version 1.2.
 * 
 * @author robmoffat
 *
 */
public class ADLDocument extends SVG12OMDocument {

	public ADLDocument() {
		this(new ADLExtensibleDOMImplementation());
	}

	public ADLDocument(ADLExtensibleDOMImplementation impl) {
		super(null, impl);
	}

	public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
		return ((ADLExtensibleDOMImplementation)implementation).createElementNS(this, namespaceURI, qualifiedName);
	}

	public Element createElement(String name) throws DOMException {
		return ((ADLExtensibleDOMImplementation)implementation).createElementNS(this, XMLHelper.KITE9_NAMESPACE, name);
	}
	
	 /**
     * Returns true if the given Attr node represents an 'id'
     * for this document.
     */
    public boolean isId(Attr node) {
        return XMLConstants.XML_ID_ATTRIBUTE.equals(node.getNodeName());
    }

    public StyleSheetList getStyleSheets() {
        throw new RuntimeException(" !!! Not implemented");
    }


    /**
     * <b>DOM</b>: Implements
     * {@link DocumentCSS#getOverrideStyle(Element,String)}.
     */
    public CSSStyleDeclaration getOverrideStyle(Element elt,
                                                String pseudoElt) {
        throw new RuntimeException(" !!! Not implemented");
    }
    
   

	@Override
	protected Node newNode() {
		return new ADLDocument();
	}

	/**
	 * Maybe move this all into the testing package?
	 */
	private transient Set<Kite9XMLElement> tempConnections = new LinkedHashSet<>();
	private boolean diagramCreated = false;

	public void setDiagramCreated(boolean diagramCreated) {
		this.diagramCreated = diagramCreated;
	}

	public void addConnection(StyledKite9SVGElement xmlElement) {
		if (diagramCreated) {
			throw new IllegalStateException("Diagram created already");
		}
		tempConnections.add(xmlElement);
	}

	public Set<Kite9XMLElement> getConnectionElements() {
		return tempConnections;
	}

	@Override
	public ADLExtensibleDOMImplementation getImplementation() {
		return (ADLExtensibleDOMImplementation) super.getImplementation();
	}

	@Override
	public Element getChildElementById(Node requestor, String id) {
		return super.getChildElementById(requestor, id);
	}
	
	
	
}
