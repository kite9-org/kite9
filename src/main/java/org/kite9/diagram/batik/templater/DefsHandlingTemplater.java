package org.kite9.diagram.batik.templater;

import java.io.IOException;

import org.apache.batik.anim.dom.SVGOMSVGElement;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.util.SVG12Constants;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.Kite9XMLElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGSVGElement;

/**
 * Provides extra functionality for ensuring that we correctly set
 * up the `<defs>` element in the output document.
 */
public class DefsHandlingTemplater extends AbstractTemplater {

	public DefsHandlingTemplater(DocumentLoader loader) {
		super(loader);
	}

	@Override
	protected ADLDocument loadReferencedDocument(String resource, Kite9XMLElement in) throws IOException {
		boolean importDefs = (loader.checkCache(resource) == null);
		ADLDocument out = super.loadReferencedDocument(resource, in);
		
		if (importDefs) {
			SVGSVGElement top = getSVGTopElement(in);
			String prefix = top.getPrefix();
			ADLDocument topDoc = (ADLDocument) top.getDocument();
			
			Element newDefs = topDoc.createElementNS(SVG12Constants.SVG_NAMESPACE_URI, SVG12Constants.SVG_DEFS_TAG);
			newDefs.setPrefix(prefix);
			top.insertBefore(newDefs, null);
			top.setAttribute("id", "defs-"+resource);
			
			NodeList defs = out.getElementsByTagNameNS(SVG12Constants.SVG_NAMESPACE_URI, SVG12Constants.SVG_DEFS_TAG);
			for (int i = 0; i < defs.getLength(); i++) {
				Element def = (Element) defs.item(i);
				XMLProcessor c = new PrefixingCopier(prefix, newDefs);
				c.processContents(def);
			}
		}
		
		return out;
	}
	
	private SVGOMSVGElement getSVGTopElement(Kite9XMLElement in) {
		return (SVGOMSVGElement) in.getOwnerDocument().getDocumentElement();
	}

	

}
