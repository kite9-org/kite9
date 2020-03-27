package org.kite9.diagram.dom.processors.pre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.AbstractStyledKite9XMLElement;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.processors.xpath.AbstractInlineProcessor;
import org.kite9.framework.common.Kite9XMLProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Handles copying of XML from one document to another, and the CSS 'template' directive.
 * 
 * Most of the "logic" is devolved to the ContentElementHandlingCopier.
 * 
 * @author robmoffat
 *
 */
public class BasicTemplater extends AbstractInlineProcessor implements XMLProcessor, Logable {
	
	protected Kite9Log log = new Kite9Log(this);
	
	@Override
	public String getPrefix() {
		return "TXML";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

	protected Kite9DocumentLoader loader;

	public BasicTemplater(Kite9DocumentLoader  loader) {
		super();
		this.loader = loader;
	}
	
	@Override
	public Element processTag(Element e) {
		super.processTag(e);

		Value v = getTemplateValue(e);
		if (v != ValueConstants.NONE_VALUE) {
			handleTemplateElement((CSSStylableElement) e, v);
		}
		
		return e;
	}
	
	public void handleTemplateElement(CSSStylableElement transform, Value v) {
		Element e = loadReferencedElement(v, transform);
		if (e != null) { 
			// template into a temporary element
			NodeValueReplacer nvr = new NodeValueReplacer(transform);
			Element temp = transform.getOwnerDocument().createElement("temp");
			copyAttributes(e, temp);
			ContentElementCopier bc = new ContentElementCopier(temp, nvr);
			bc.processContents(e);
			
			// remove the original contents
			NodeList childNodes = transform.getChildNodes();
			while (childNodes.getLength() > 0) {
				transform.removeChild(childNodes.item(0));
			}
			
			copyAttributes(temp, transform);
			moveContents(temp, transform);
			
			//System.out.println("finished BasicTemplater: "+transform.getLocalName());
		} else {
			throw new Kite9XMLProcessingException("Couldn't resolve template: " + getTemplateName(v), transform);
		}
	}
	
	private void moveContents(Element from, Element to) {
		NodeList in = from.getChildNodes();
		while (in.getLength() > 0) {
			to.appendChild(in.item(0));
		}		
	}

	private Value getTemplateValue(Element e) {
		if (e instanceof CSSStylableElement) {
			Value template = AbstractStyledKite9XMLElement.getCSSStyleProperty((CSSStylableElement) e, CSSConstants.TEMPLATE);
			return template;
		} else {
			return ValueConstants.NONE_VALUE;
		}
	}

	private static String getTemplateName(Value t) {
		if (t instanceof ListValue) {
			return t.item(0).getStringValue();
		} else {
			return t.getStringValue();
		}
	}

	protected void copyAttributes(Element from, Element to) {
        NamedNodeMap attributes = from.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr node = (Attr) attributes.item(i);
            if ((to.getAttribute(node.getName()).length() == 0) && (!node.getName().equals("id"))) {
            	to.setAttribute(node.getName(), node.getValue());
            }
        }
    }

	protected Element loadReferencedElement(Value v, Element usedIn) {
		return loader.loadElementFromUrl(v, usedIn);
	}


	public static List<String> getParameters(Value v) {
		if (v instanceof ListValue) {
			List<String> out = new ArrayList<>(v.getLength()-1);
			for (int i = 1; i < v.getLength(); i++) {
				out.add(v.item(i).getStringValue());
			}
			return out;
		} else {
			return Collections.emptyList();
		}
	}
	
}
