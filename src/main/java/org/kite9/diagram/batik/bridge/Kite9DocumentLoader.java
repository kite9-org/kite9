package org.kite9.diagram.batik.bridge;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.util.SVG12Constants;
import org.kite9.diagram.batik.templater.PrefixingCopier;
import org.kite9.diagram.batik.templater.XMLProcessor;
import org.kite9.framework.dom.Kite9DocumentFactory;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.Kite9XMLElement;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * This contains functionality to load in <defs> from any referenced documents.
 * 
 * @author robmoffat
 *
 */
public class Kite9DocumentLoader extends DocumentLoader implements Logable {
	
	private final boolean importDefs;
	private final Kite9Log log = new Kite9Log(this);

	public Kite9DocumentLoader(UserAgent userAgent, Kite9DocumentFactory dbf, boolean importDefs) {
		super(userAgent);
		this.documentFactory = dbf;
		this.importDefs = importDefs;
	}

	/**
	 * Returns a given element from a url(file#id) url-string in CSS.
	 * Returns null if it can't be loaded for some reason 
	 */
	public Element loadElementFromUrl(Value v, StyledKite9SVGElement loadedBy) {
		if (v != ValueConstants.NONE_VALUE) {
			Element out;
			try {
				String resource = getUrlForDocument(v);
				String fragment = getIdentifierForElement(v);
				boolean importDefsForThisDoc = this.importDefs && (checkCache(resource) == null);

				ADLDocument templateDoc = (ADLDocument) loadDocument(resource);
				
				if (fragment != null) {
					out = templateDoc.getElementById(fragment);
				} else {
					out = templateDoc.getRootElement();
				}

				
				if (importDefsForThisDoc) {
					SVGSVGElement top = getSVGTopElement(loadedBy);
					String prefix = top.getPrefix();
					ADLDocument topDoc = (ADLDocument) top.getDocument();

					Element newDefs = topDoc.createElementNS(SVG12Constants.SVG_NAMESPACE_URI, SVG12Constants.SVG_DEFS_TAG);
					newDefs.setPrefix(prefix);
					top.insertBefore(newDefs, null);
					top.setAttribute("id", "defs-" + resource);

					NodeList defs = out.getOwnerDocument().getElementsByTagNameNS(SVG12Constants.SVG_NAMESPACE_URI, SVG12Constants.SVG_DEFS_TAG);
					for (int i = 0; i < defs.getLength(); i++) {
						Element def = (Element) defs.item(i);
						XMLProcessor c = new PrefixingCopier(prefix, newDefs);
						c.processContents(def);
					}
				}

				return out;

			} catch (Exception e) {
				log.error("Couldn't load element: "+v.getStringValue(), e);
				return null;
			}
		} else {
			return null;
		}
	}

	private SVGOMSVGElement getSVGTopElement(Kite9XMLElement in) {
		return (SVGOMSVGElement) in.getOwnerDocument().getDocumentElement();
	}
	
	public static String getIdentifierForElement(Value v) throws URISyntaxException {
		String uri = v.getStringValue();
		URI u2 = new URI(uri);
		String fragment = u2.getFragment();
		return fragment;
	}

	public static String getUrlForDocument(Value v) throws URISyntaxException {
		String uri = v.getStringValue();

		// identify the fragment referenced in the other document
		// and load it
		URI u = new URI(uri);
		String resource = u.getScheme() + ":" + u.getSchemeSpecificPart();
		return resource;
	}

	@Override
	public String getPrefix() {
		return "K9DL";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
	
}