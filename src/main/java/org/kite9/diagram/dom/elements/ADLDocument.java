package org.kite9.diagram.dom.elements;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.batik.anim.dom.SVG12OMDocument;
import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSStyleSheetNode;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.xpath.XPathContext;
import org.kite9.diagram.dom.ADLExtensibleDOMImplementation;
import org.kite9.diagram.dom.CachingSVGDOMImplementation;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.defs.HasDefs;
import org.kite9.diagram.dom.defs.DefList;
import org.kite9.diagram.dom.processors.pre.TemplateAwareVariableStack;
import org.kite9.diagram.dom.processors.xpath.XPathAware;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.DocumentCSS;
import org.w3c.dom.xpath.XPathException;
import org.w3c.dom.xpath.XPathExpression;
import org.w3c.dom.xpath.XPathNSResolver;


/**
 * Now, Kite9 elements are first-class members of SVG, and support SVG version 1.2.
 * 
 * @author robmoffat
 *
 */
public class ADLDocument extends SVG12OMDocument implements XPathAware, HasDefs {

	public ADLDocument() {
		this(new ADLExtensibleDOMImplementation());
	}

	public ADLDocument(ADLExtensibleDOMImplementation impl) {
		super(null, impl);
	}

	public Element createElementNS(String namespaceURI, String qualifiedName) throws DOMException {
		return ((CachingSVGDOMImplementation)implementation).createElementNS(this, namespaceURI, qualifiedName);
	}

	public Element createElement(String name) throws DOMException {
		return ((CachingSVGDOMImplementation)implementation).createElementNS(this, XMLHelper.KITE9_NAMESPACE, name);
	}
	
	public boolean elementIdExists(String id) {
		if (elementsById == null) {
			return false;
		}
		return elementsById.containsKey(id);
	}
	
	private transient int nextId = 1;
	
	public String createUniqueId() {
		while (elementIdExists(""+(nextId))) {
			nextId ++;
		}
		
		return ""+nextId;
	}
	
	/**
     * Returns true if the given Attr node represents an 'id'
     * for this document.
     */
    public boolean isId(Attr node) {
        return "id".equals(node.getNodeName());
    }

//    public StyleSheetList getStyleSheets() {
//        throw new RuntimeException(" !!! Not implemented");
//    }
//    
    private DefList scriptList;
    
    public DefList getImportList() {
    	if (scriptList == null) {
    		@SuppressWarnings("unchecked")
			List<CSSStyleSheetNode> nodes = (List<CSSStyleSheetNode>) getCSSEngine().getStyleSheetNodes();
			List<StyleSheet> list = nodes.stream()
    			.map(o -> ((CSSStyleSheetNode)o).getCSSStyleSheet())
    			.collect(Collectors.toList());
    		
    		scriptList = new DefList(list);
     	} 
    	
    	return scriptList;
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

	public void addConnection(StyledKite9XMLElement xmlElement) {
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

	/**
	 * Overrides the basic implementation to provide {@link XPathAware} support
	 */
	@Override
	public Object evaluate(String expression, Node contextNode, XPathNSResolver resolver, short type, Object result) throws XPathException, DOMException {
		if (resolver == null) {
			resolver = (prefix) -> {
				if (prefix.equals("")) {
					return contextNode.getOwnerDocument().getNamespaceURI();
				} else if (prefix.equals("adl")) {
					return XMLHelper.KITE9_NAMESPACE;
				} else {
					return contextNode.lookupNamespaceURI(prefix);
				}
			};
		}
		
		try {
			XPathExpression xpath = createExpression(expression, resolver);
			ADLXPathExpr expr = (ADLXPathExpr) xpath;
			expr.getContext().setVarStack(new TemplateAwareVariableStack(10, contextNode));
			return xpath.evaluate(contextNode, type, result);
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("XPath Evaluation failed with expression '"+expression+"'", e, contextNode);
		}
	}
	
	
	public static final Set<String> UNITS = new HashSet<>();
	
	static {
		for (String string : new String[] { "pt", "cm", "em", "in", "ex", "px" }) {
			UNITS.add(string);
		}
	}	

	/**
	 * Because our ADLDocument knows about the CSSContext, we can resolve units in the xpath expressions.
	 */
	@Override
	public String getXPathVariable(String name) {
		if (UNITS.contains(name)) {
			CSSContext ctx = this.getCSSEngine().getCSSContext();
			UnitProcessor.Context me = new UnitProcessor.Context() {

				public Element getElement() { return null; }
				public float getFontSize() { return 0; }
				public float getXHeight() { return 0; }
				public float getViewportHeight() {return 0;}
				public float getViewportWidth() {return 0;}

				public float getPixelUnitToMillimeter() {
					return ctx.getPixelUnitToMillimeter();
				}

				public float getPixelToMM() {
					return ctx.getPixelToMillimeter();
				}
				
			};
			
			float f = UnitProcessor.svgToUserSpace("1"+name, "", UnitProcessor.HORIZONTAL_LENGTH, me);
			return ""+f;
		}

		return null;
	}


	protected class ADLXPathExpr extends XPathExpr {

		public ADLXPathExpr(String expr, XPathNSResolver res) throws DOMException, XPathException {
			super(expr, res);
		}

		public XPathContext getContext() {
			return context;
		}	
	}
	
	@Override
	public XPathExpression createExpression(String expression, XPathNSResolver resolver) throws DOMException, XPathException {
		 return new ADLXPathExpr(expression, resolver);
	}
}
