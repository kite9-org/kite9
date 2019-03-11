package org.kite9.diagram.dom.processors.template;

import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.elements.AbstractStyleableXMLElement;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.processors.xpath.ValueReplacingProcessor;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

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

	public BasicTemplater(ValueReplacer vr, Kite9DocumentLoader  loader) {
		super(vr);
		this.loader = loader;
	}

	public void handleTemplateElement(CSSStylableElement transform) {
		Value template = AbstractStyleableXMLElement.getCSSStyleProperty(transform, CSSConstants.TEMPLATE);
		if (template != ValueConstants.NONE_VALUE) {
			Element e = loadReferencedElement(template, transform);
			if (e != null) { 
				ContentElementHandlingCopier c = new ContentElementHandlingCopier(transform);
				copyAttributes(e, transform);
				c.processContents(e);

				log.send("Templated: (" + template.getStringValue() + ")\n" + new XMLHelper().toXML(transform));
			} else {
				throw new Kite9ProcessingException("Couldn't resolve template: " + template.getStringValue());
			}
		}
		
	}
	

	protected void copyAttributes(Element from, Element to) {
        NamedNodeMap attributes = from.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr node = (Attr) attributes.item(i);
            if (!node.isId()) {
            	to.setAttribute(node.getName(), node.getValue());
            }
        }
    }

	protected Element loadReferencedElement(Value v, Element usedIn) {
		return loader.loadElementFromUrl(v, usedIn);
	}

	@Override
	public void processElement(Element e) {
		if (e instanceof CSSStylableElement) {
			handleTemplateElement((CSSStylableElement) e);
		}
		super.processElement(e);
	}
	
}
