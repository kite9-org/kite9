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
import org.kite9.diagram.dom.processors.copier.BasicCopier;
import org.kite9.diagram.dom.processors.xpath.ContentElementProcessor;
import org.kite9.diagram.dom.processors.xpath.NodeValueReplacer;
import org.kite9.diagram.dom.processors.xpath.ValueReplacer;
import org.kite9.diagram.dom.processors.xpath.ValueReplacingProcessor;
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
public class BasicTemplater extends ValueReplacingProcessor implements XMLProcessor, Logable {
	
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

	public BasicTemplater(ValueReplacer vr, Kite9DocumentLoader  loader) {
		super(vr);
		this.loader = loader;
	}
	
	protected XMLProcessor subInstance(ValueReplacer vr) {
		BasicTemplater out = new BasicTemplater(vr, loader);
		out.ignoreElement = true;	// prevents templating the same element over and over again.
		return out;
	}

	public void handleTemplateElement(CSSStylableElement transform, Value v) {
		Element e = loadReferencedElement(v, transform);
		if (e != null) { 
			//System.out.println("BasicTemplater: "+transform.getLocalName());
			// we need to create a copy of this element, in the same document.
			Element copy = copyNodeAndMoveContents(transform);
			NodeValueReplacer nvr = new NodeValueReplacer(copy);
			
			// move the new contents in
			BasicCopier bc = new BasicCopier(transform);
			bc.processContents(e);
			
			XMLProcessor subProcessor = subInstance(nvr);
			copyAttributes(e, transform);
			subProcessor.processContents(transform);
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

	@Override
	public void processTag(Element e) {
		super.processTag(e);

		if (e instanceof HasPreprocessor) {
			((HasPreprocessor)e).setPreprocessor(this);
		}

		if (!ignoreElement) {
			Value v = getTemplateValue(e);
			if (v != ValueConstants.NONE_VALUE) {
				handleTemplateElement((CSSStylableElement) e, v);
			}
		}

		ignoreElement = false;
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
