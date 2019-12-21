package org.kite9.diagram.dom.processors.pre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.dom.CSSConstants;
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
	protected boolean ignoreElement = false;

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
			//System.out.println("BasicTemplater: "+transform.getLocalName());
			// we need to create a copy of this element, in the same document.
			Element copy = copyNodeAndMoveContents(transform);
			NodeValueReplacer nvr = new NodeValueReplacer(copy);
			String prefix = transform.getOwnerDocument().getDocumentElement().getPrefix();
			
			// move the new contents in
			ContentElementCopier bc = new ContentElementCopier(transform, prefix, nvr);
			bc.processContents(e);
			//System.out.println("finished BasicTemplater: "+transform.getLocalName());
		} else {
			throw new Kite9XMLProcessingException("Couldn't resolve template: " + getTemplateName(v), transform);
		}
	}
		
	private Element copyNodeAndMoveContents(CSSStylableElement original) {
		
		Element out = (Element) original.cloneNode(false);
		copyAttributes(original, out);
		
		NodeList in = original.getChildNodes();
		while (in.getLength() > 0) {
			out.appendChild(in.item(0));
		}		
		return out;
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
            if ((!node.isId()) && (to.getAttribute(node.getName()).length() == 0)) {
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
