package org.kite9.diagram.dom.processors.pre;

import javax.xml.transform.TransformerException;

import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.AbstractStyledKite9XMLElement;
import org.kite9.diagram.dom.processors.xpath.XPathAwareVariableStack;
import org.kite9.framework.common.Kite9XMLProcessingException;
import org.w3c.dom.Node;

public class TemplateAwareVariableStack extends XPathAwareVariableStack {

	public TemplateAwareVariableStack(int initStackSize, Node context) {
		super(initStackSize, context);
	}

	@Override
	public XObject getVariableOrParam(XPathContext arg0, QName name) throws TransformerException {
		String key = name.toNamespacedString();
		if (key.matches("^template-[0-9]+$")) {
			Node current = context;
			int arg = Integer.parseInt(key.substring(9));
			while (current != null) {
				Value v = AbstractStyledKite9XMLElement.getCSSStyleProperty((CSSStylableElement) current, CSSConstants.TEMPLATE);
						
				if ((v instanceof ListValue) && (v.getLength() > arg)) {
					ListValue found = (ListValue) v;
					return XObject.create(found.item(arg).getStringValue());
				} 
				
				current = current.getParentNode();
			}
			
			throw new Kite9XMLProcessingException("Couldn't resolve template variable: "+key, context);
		} else  {
			return super.getVariableOrParam(arg0, name);
		}
	}
}

