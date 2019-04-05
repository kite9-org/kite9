package org.kite9.diagram.dom.elements;

import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.kite9.diagram.dom.CSSConstants;
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
		if (key.matches("^template-[0-9]+$")) {
			Node current = this;
			int arg = Integer.parseInt(key.substring(9));
			ListValue found = null;
			while ((found == null) && (current != null)) {
				if (current instanceof StyledKite9XMLElement) {
					Value v = getCSSStyleProperty((StyledKite9XMLElement) current, CSSConstants.TEMPLATE);
					if (v instanceof ListValue) {
						found = (ListValue) v;
					} else if (v == ValueConstants.NONE_VALUE) {
						// continue searching
					} else {
						// unparameterized template
						return null;
					}
				}
				
				current = current.getParentNode();
			}
			
			if ((found != null) && (found.getLength() > arg)) {
				return found.item(arg).getStringValue();
			} else {
				return null;
			}
		} else if (getDiagramElement() instanceof XPathAware) {
			String out = ((XPathAware) getDiagramElement()).getXPathVariable(key);
			return out;
		} else {
			return null;
		}
	}
}
