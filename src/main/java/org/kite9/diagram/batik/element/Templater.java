package org.kite9.diagram.batik.element;

import java.io.IOException;
import java.net.URI;

import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.util.XMLConstants;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.xml.ADLDocument;
import org.kite9.framework.xml.XMLElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Templater {
	
	private DocumentLoader loader;

	public Templater(DocumentLoader loader) {
		this.loader = loader;
	}

	/**
	 * This needs to copy the template XML source into the destination.
	 */
	public void handleTemplateElement(XMLElement in, DiagramElement o) {
		if (o instanceof AbstractXMLDiagramElement) {
			AbstractXMLDiagramElement out = (AbstractXMLDiagramElement) o;
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
					ADLDocument templateDoc = loadReferencedDocument(resource);
					Element e = templateDoc.getElementById(fragment);

					Node copy = copyIntoDocument(in, e);

					// ensure xml:base is set so references work in the copied
					// content
					((Element) copy).setAttributeNS(XMLConstants.XML_NAMESPACE_URI, XMLConstants.XML_BASE_ATTRIBUTE, resource);

				} catch (Exception e) {
					throw new Kite9ProcessingException("Couldn't resolve template: " + uri, e);
				}
			}
		}
	}

	public static Node copyIntoDocument(XMLElement in, Element e) {
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
	
	public static void insertCopyBefore(Node before, Element contentsOf) {
		Element into = (Element) before.getParentNode();
		ADLDocument thisDoc = (ADLDocument) into.getOwnerDocument();
		NodeList toCopy = contentsOf.getChildNodes();
		for (int i = toCopy.getLength()-1; i >=0; i--) {
			Node e = toCopy.item(i);
			Node copy = e.cloneNode(true);
			thisDoc.adoptNode(copy);
			into.insertBefore(copy, before);
		}
	}
	
	
	private ADLDocument loadReferencedDocument(String resource) throws IOException {
		return (ADLDocument) loader.loadDocument(resource);
	}

}
