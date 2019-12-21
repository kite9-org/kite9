package org.kite9.diagram.dom.elements;

import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.dom.processors.xpath.XPathAware;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathResult;

public abstract class AbstractReferencingKite9XMLElement extends AbstractStyledKite9XMLElement implements ReferencingKite9XMLElement, XPathAware {

	public AbstractReferencingKite9XMLElement() {
		super();
	}

	public AbstractReferencingKite9XMLElement(String name, ADLDocument owner) {
		super(name, owner);
	}
	
	@Override
	public String getIDReference(String referenceName) {
		return ((XPathResult) evaluate(referenceName, XPathResult.STRING_TYPE)).getStringValue();
	}
	
	@Override
	public void setIDReference(String referenceName, String value) {
		XPathResult result = (XPathResult) evaluate(referenceName, XPathResult.ANY_UNORDERED_NODE_TYPE);
		Node n = result.getSingleNodeValue();
		n.setTextContent(value);
	}

	@Override
	public Node getNode(String referenceName) {
		return ((XPathResult) evaluate(referenceName, XPathResult.ANY_UNORDERED_NODE_TYPE)).getSingleNodeValue();
	}

	public Object evaluate(String referenceName, short type) {
		Value v = getCSSStyleProperty(referenceName);
		if (v instanceof StringValue) {
			String xpath = v.getStringValue();
			Object out = getOwnerDocument().evaluate(xpath, this, null, type, null);	
			return out;
		} else {
			return null;
		}
	}
	
	/**
	 * Matches template-n variables, and also defers to the diagram element for variable resolution
	 * in the case of decals.
	 */
	@Override
	public String getXPathVariable(String key) {
		if (getDiagramElement() instanceof XPathAware) {
			String out = ((XPathAware) getDiagramElement()).getXPathVariable(key);
			return out;
		} else {
			return null;
		}
	}
}
