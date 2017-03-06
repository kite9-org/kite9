package org.kite9.diagram.visualization.batik.bridge;

import java.io.IOException;
import java.net.URI;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.svg12.SVG12BridgeContext;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLConstants;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.xml.ADLDocument;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.serialization.CSSConstants;
import org.kite9.framework.serialization.Kite9DocumentFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The Kite9 bridge context has to manage the conversion of XML elements into {@link GraphicsNode} 
 * contents.   Since we also have `template` functionality now, it also has to manage loading 
 * templates correctly, and we'll use the {@link DocumentLoader} to handle this.
 * 
 * @author robmoffat
 *
 */
public final class Kite9BridgeContext extends SVG12BridgeContext {
	
	public Kite9BridgeContext(UserAgent userAgent, DocumentLoader loader) {
		super(userAgent, loader);
	}
	
	static class Kite9DocumentLoader extends DocumentLoader {

		public Kite9DocumentLoader(UserAgent userAgent, Kite9DocumentFactory dbf) {
			super(userAgent);
			this.documentFactory = dbf;
		}
		
	}

	public Kite9BridgeContext(UserAgent userAgent, Kite9DocumentFactory dbf) {
		this(userAgent, new Kite9DocumentLoader(userAgent, dbf));
	}

	public boolean isInteractive() {
		return false;
	}

	public boolean isDynamic() {
		return false;
	}
	

	@Override
	public void registerSVGBridges() {
		super.registerSVGBridges();
		putBridge(new Kite9DiagramGroupBridge(this));
		putBridge(new Kite9GBridge());
		putBridge(new TextBridge());
	}
	
	
	/**
	 * This needs to copy the template XML source into the destination.
	 */
	public void handleTemplateElement(XMLElement in, DiagramElement out) {
		Value template = out.getCSSStyleProperty(CSSConstants.TEMPLATE);
		if (template != ValueConstants.NONE_VALUE) {
			String uri = template.getStringValue();

			try {
				// identify the fragment referenced in the other document and
				// load it
				URI u = new URI(uri);
				String fragment = u.getFragment();
				String resource = u.getScheme() + ":" + u.getSchemeSpecificPart();
				ADLDocument templateDoc = loadReferencedDocument(resource);
				Element e = templateDoc.getElementById(fragment);

				Node copy = copyIntoDocument(in, e);
				
				// ensure xml:base is set so references work in the copied content
				((Element)copy).setAttributeNS(XMLConstants.XML_NAMESPACE_URI, XMLConstants.XML_BASE_ATTRIBUTE, resource);

			} catch (Exception e) {
				throw new Kite9ProcessingException("Couldn't resolve template: " + uri, e);
			}
		}
	}

	private Node copyIntoDocument(XMLElement in, Element e) {
		// copy this element into the new document
		Node copy = e.cloneNode(true);
		ADLDocument thisDoc = in.getOwnerDocument();
		thisDoc.adoptNode(copy);
		
		if (in.getChildXMLElementCount() == 0) {
			in.appendChild(copy);
		} else {
			XMLElement first = in.iterator().next();
			in.insertBefore(copy, first);
		}
		return copy;
	}
	
	private ADLDocument loadReferencedDocument(String resource) throws IOException {
		return (ADLDocument) getDocumentLoader().loadDocument(resource);
	}


}