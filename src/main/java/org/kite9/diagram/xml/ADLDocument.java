package org.kite9.diagram.xml;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.util.XMLConstants;
import org.kite9.framework.serialization.ADLExtensibleDOMImplementation;
import org.kite9.framework.serialization.XMLHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.stylesheets.StyleSheetList;

/**
 * NOTE:  It would be better not to extend SVG12OMDocument, and extend AbstractStyleableDocument,
 * but CSSUtilities does lots of casting to SVGOMDocument, and we want to use that in the
 * kite9-visualisation project.
 * 
 * Now, Kite9 elements are first-class members of SVG.
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
	private transient Set<XMLElement> tempConnections = new LinkedHashSet<>();
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

	public Set<XMLElement> getConnectionElements() {
		return tempConnections;
	}

	@Override
	public ADLExtensibleDOMImplementation getImplementation() {
		return (ADLExtensibleDOMImplementation) super.getImplementation();
	}
	
}
