package org.kite9.diagram.dom.processors.copier;

import javax.xml.XMLConstants;

import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.processors.xpath.AbstractProcessor;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Contains basic functionality for copying the contents of one XML node 
 * into another.
 * 
 * @author robmoffat
 *
 */
public class BasicCopier extends AbstractProcessor {
	
	protected Node destination;
	
	public BasicCopier(Node destination) {
		this.destination = destination;
	}

	@Override
	public Node processContents(Node from) {
		return processContents(from, destination);
	}

	@Override
	protected Element processTag(Element n) {
		return (Element) processNode(n);
	}
	
	@Override
	protected Text processText(Text n) {
		return (Text) processNode(n);
	}


	private Node processNode(Node n) {
		Node copy = n.cloneNode(false);
		
		Document ownerDocument = getDestinationDocument();
		ownerDocument.adoptNode(copy);
		
		if (copy instanceof Element) {
			removeExtraneousNamespaces((Element) copy);
			checkIDElement((Element) copy);
		}
		
		return copy;
	}

	protected Document getDestinationDocument() {
		return destination instanceof Document ? (Document) destination : destination.getOwnerDocument();
	}

	private void removeExtraneousNamespaces(Element copy) {
		NamedNodeMap nnm = copy.getAttributes();
		
		if (nnm == null) {
			return;
		}
		
		int i = 0;
		while (i < nnm.getLength()) {
			Attr a = (Attr) nnm.item(i);
			String ns = a.getNamespaceURI();
			
			if (shouldRemoveNamespace(ns)) {
				nnm.removeNamedItemNS(ns, a.getLocalName());
			} else {
				i++;
			}
		}
	}

	protected boolean shouldRemoveNamespace(String ns) {
		return (ns != null) && (!ns.equals("")) 
				&& (!ns.equals(SVGConstants.SVG_NAMESPACE_URI)) 
				&& (!ns.equals(XMLHelper.KITE9_NAMESPACE))
				&& (!ns.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
				&& (!ns.equals(SVGConstants.XLINK_NAMESPACE_URI))
				&& (!ns.equals(XMLConstants.XML_NS_URI));
	}

	/**
	 * Forces re-indexing the IDs within the destination document
	 */
	private void checkIDElement(Element copy) {
		if (copy.hasAttribute("id")) {
			String id = copy.getAttribute("id");
			((SVGOMDocument) copy.getOwnerDocument()).updateIdEntry(copy, null, id);
		}
	}



}
