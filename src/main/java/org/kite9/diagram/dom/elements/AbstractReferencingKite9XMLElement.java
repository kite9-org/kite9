package org.kite9.diagram.dom.elements;

import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.dom.XMLHelper;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathNSResolver;
import org.w3c.dom.xpath.XPathResult;

public abstract class AbstractReferencingKite9XMLElement extends AbstractStyledKite9XMLElement implements ReferencingKite9XMLElement {

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
			Object out = getOwnerDocument().evaluate(xpath, this, new XPathNSResolver() {
				
				@Override
				public String lookupNamespaceURI(String arg0) {
					if (arg0.equals("adl")) {
						return XMLHelper.KITE9_NAMESPACE;
					} else {
						return "";
					}
				}
			}, type, null);	
			return out;
		} else {
			return null;
		}
	}
}
