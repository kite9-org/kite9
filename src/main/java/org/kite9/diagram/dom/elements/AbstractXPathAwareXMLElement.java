package org.kite9.diagram.dom.elements;

import java.util.Collection;
import java.util.Collections;

import org.apache.batik.dom.AbstractAttr;
import org.apache.batik.dom.AbstractDocument;
import org.kite9.diagram.dom.processors.xpath.XPathAware;
import org.kite9.diagram.model.DiagramElement;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This contains additional functionality which exposes attributes from the {@link DiagramElement}
 * for XPath expressions to use.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractXPathAwareXMLElement extends AbstractStyleableXMLElement implements XPathAware {

	public AbstractXPathAwareXMLElement() {
		super();
	}

	public AbstractXPathAwareXMLElement(String name, ADLDocument owner) {
		super(name, owner);
	}
	
	

	@Deprecated
	@Override
	public String getXPathVariable(String key) {
		if (getDiagramElement() instanceof XPathAware) {
			return ((XPathAware) getDiagramElement()).getXPathVariable(key);
		}
		
		return null;
	}
	
	@Override
	public Collection<String> getXPathVariables() {
		if (getDiagramElement() instanceof XPathAware) {
			return ((XPathAware) getDiagramElement()).getXPathVariables();
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Provides support for attributes derived from the diagram element.
	 */
	@Override
	public DiagramElement getDiagramElement() {
		boolean addAttributes = cachedDiagramElement == null;
		DiagramElement out = super.getDiagramElement();
		
		if ((addAttributes) &&  (getDiagramElement() instanceof XPathAware)) {
			XPathAware de = (XPathAware) getDiagramElement();
			for (String a : de.getXPathVariables()) {
				setAttributeNode(new ReactiveAttr(a, this.getOwnerDocument(), de));
			}
		}
		
		return out;
	}

	static class ReactiveAttr extends AbstractAttr {
				
		private XPathAware xpathAware;
		
		ReactiveAttr(String name, AbstractDocument d, XPathAware xPathAware) {
			super(name, d);
	        setNodeName(name);
	        this.xpathAware = xPathAware;
		}

		@Override
		public boolean isReadonly() {
			return false;
		}

		@Override
		public void setReadonly(boolean v) {
		}

		@Override
		public String getNodeValue() throws DOMException {
			String name = getName();
			String out = xpathAware.getXPathVariable(name);
			System.out.println(name+" "+out);;
			return out;
		}

		@Override
		protected Node newNode() {
			return new ReactiveAttr(getNodeName(), getCurrentDocument(), xpathAware);
		}
		
	}
	
}
