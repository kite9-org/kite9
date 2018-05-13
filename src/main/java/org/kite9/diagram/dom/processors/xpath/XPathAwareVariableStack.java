package org.kite9.diagram.dom.processors.xpath;

import javax.xml.transform.TransformerException;

import org.apache.xml.utils.QName;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.kite9.framework.common.Kite9ProcessingException;
import org.w3c.dom.Node;

public class XPathAwareVariableStack extends VariableStack {
	
	private Node context;

	public XPathAwareVariableStack(Node context) {
		super();
		this.context = context;
	}

	public XPathAwareVariableStack(int initStackSize, Node context) {
		super(initStackSize);
		this.context = context;
	}

	@Override
	public XObject getVariableOrParam(XPathContext arg0, QName name) throws TransformerException {
		Node n = context;
		String key = name.toNamespacedString();

		while (n != null) {
			if (n instanceof XPathAware) {
				String out = ((XPathAware) n).getXPathVariables().get(key);
				if (out != null) {
					return XObject.create(out);
				}
			}
			
			n = n.getParentNode();
		}
		
		return XObject.create("");
	}

	
}
