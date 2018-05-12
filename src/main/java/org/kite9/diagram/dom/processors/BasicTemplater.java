package org.kite9.diagram.dom.processors;

import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.kite9.diagram.batik.bridge.Kite9DocumentLoader;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.w3c.dom.Element;

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

	public void handleTemplateElement(StyledKite9SVGElement transform) {
		Value template = transform.getCSSStyleProperty(CSSConstants.TEMPLATE);
		if (template != ValueConstants.NONE_VALUE) {
			Element e = loadReferencedElement(template, transform);
			if (e != null) { 
				ContentElementHandlingCopier c = new ContentElementHandlingCopier(transform);
				c.processContents(e);

				log.send("Templated: (" + template.getStringValue() + ")\n" + new XMLHelper().toXML(transform));
			} else {
				throw new Kite9ProcessingException("Couldn't resolve template: " + template.getStringValue());
			}
		}
		
	}

	protected Element loadReferencedElement(Value v, StyledKite9SVGElement usedIn) {
		return loader.loadElementFromUrl(v, usedIn);
	}

	@Override
	public void processElement(Element e) {
		if (e instanceof StyledKite9SVGElement) {
			handleTemplateElement((StyledKite9SVGElement) e);
		}
		
		super.processElement(e);
	}
	
}
