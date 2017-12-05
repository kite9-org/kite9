package org.kite9.diagram.batik.templater;

import java.io.IOException;
import java.net.URI;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.kite9.diagram.batik.element.AbstractDOMDiagramElement;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.XMLHelper;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.Kite9XMLElement;
import org.w3c.dom.Element;

/**
 * Handles copying of XML from one document to another, and the CSS 'template' directive.
 * 
 * Most of the "logic" is devolved to the subclasses.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractTemplater implements Templater, Logable {
	
	protected Kite9Log log = new Kite9Log(this);
	
	@Override
	public String getPrefix() {
		return "TXML";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

	protected DocumentLoader loader;

	public AbstractTemplater(DocumentLoader loader) {
		this.loader = loader;
	}

	@Override
	public void handleTemplateElement(Kite9XMLElement in, DiagramElement o) {
		if (o instanceof AbstractDOMDiagramElement) {
			AbstractDOMDiagramElement out = (AbstractDOMDiagramElement) o;
			Value template = out.getCSSStyleProperty(CSSConstants.TEMPLATE);
			if (template != ValueConstants.NONE_VALUE) {
				String uri = template.getStringValue();

				try {
					// identify the fragment referenced in the other document
					// and
					// load it
					URI u = new URI(uri);
					String fragment = u.getFragment();
					String resource = u.getScheme() + ":" + u.getSchemeSpecificPart();
					ADLDocument templateDoc = loadReferencedDocument(resource, in);
					Element e = templateDoc.getElementById(fragment);
					
					ContentElementHandlingCopier c = new ContentElementHandlingCopier(in);
					c.processContents(e);
					
					log.send("Templated: ("+fragment+")\n"+new XMLHelper().toXML(in));
					
				} catch (Exception e) {
					throw new Kite9ProcessingException("Couldn't resolve template: " + uri, e);
				}
			}
		}
	}

	protected ADLDocument loadReferencedDocument(String resource, @SuppressWarnings("unused") Kite9XMLElement in) throws IOException {
		return (ADLDocument) loader.loadDocument(resource);
	}
}
